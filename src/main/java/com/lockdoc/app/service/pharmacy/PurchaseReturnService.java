package com.lockdoc.app.service.pharmacy;

import com.lockdoc.app.config.SecurityUtils;
import com.lockdoc.app.dto.PageResponse;
import com.lockdoc.app.dto.pharmacy.PurchaseReturnItemRequest;
import com.lockdoc.app.dto.pharmacy.PurchaseReturnRequest;
import com.lockdoc.app.dto.pharmacy.PurchaseReturnResponse;
import com.lockdoc.app.entity.Facility;
import com.lockdoc.app.entity.pharmacy.MedicineBatch;
import com.lockdoc.app.entity.pharmacy.Purchase;
import com.lockdoc.app.entity.pharmacy.PurchaseItem;
import com.lockdoc.app.entity.pharmacy.PurchaseReturn;
import com.lockdoc.app.entity.pharmacy.PurchaseReturnItem;
import com.lockdoc.app.entity.pharmacy.StockLedgerEntry;
import com.lockdoc.app.exception.InsufficientStockException;
import com.lockdoc.app.exception.InvalidDocumentStateException;
import com.lockdoc.app.exception.ResourceNotFoundException;
import com.lockdoc.app.repository.pharmacy.MedicineBatchRepository;
import com.lockdoc.app.repository.pharmacy.PurchaseItemRepository;
import com.lockdoc.app.repository.pharmacy.PurchaseRepository;
import com.lockdoc.app.repository.pharmacy.PurchaseReturnItemRepository;
import com.lockdoc.app.repository.pharmacy.PurchaseReturnRepository;
import com.lockdoc.app.repository.pharmacy.StockLedgerEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A debit note against an already-accepted GRN batch (Master Spec §11.3)
 * - damaged, expired, wrong supply, rate dispute. Distinct from
 * cancelling a purchase (PurchaseService.cancel, a same-day-mistake
 * undo). Capped at what remains un-returned on the GRN line, and at
 * what's still physically on hand (the stock must exist to send back).
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PurchaseReturnService {

    private static final String DOC_TYPE = "PURCHASE_RETURN";
    private static final String PREFIX = "PRET";
    private static final String TXN_PURCHASE_RETURN = "PURCHASE_RETURN";

    private final PurchaseReturnRepository purchaseReturnRepository;
    private final PurchaseReturnItemRepository purchaseReturnItemRepository;
    private final PurchaseRepository purchaseRepository;
    private final PurchaseItemRepository purchaseItemRepository;
    private final MedicineBatchRepository medicineBatchRepository;
    private final StockLedgerEntryRepository stockLedgerEntryRepository;
    private final DocumentNumberService documentNumberService;

    public PageResponse<PurchaseReturnResponse> list(int page, int size) {
        Long facilityId = SecurityUtils.requireFacilityId();
        Page<PurchaseReturn> result = purchaseReturnRepository.findByFacilityId(facilityId, PageRequest.of(page, size));
        return PageResponse.of(result, PurchaseReturnResponse::toResponse);
    }

    public PurchaseReturnResponse get(Long id) {
        return PurchaseReturnResponse.toResponse(findEntity(id));
    }

    public PurchaseReturnResponse create(PurchaseReturnRequest request, Long userId) {
        Long facilityId = SecurityUtils.requireFacilityId();

        Purchase purchase = purchaseRepository.findByIdAndFacilityId(request.getPurchaseId(), facilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase not found with id: " + request.getPurchaseId()));
        if (!"POSTED".equals(purchase.getStatus())) {
            throw new InvalidDocumentStateException("Cannot return against a purchase in status: " + purchase.getStatus());
        }
        Facility facility = purchase.getFacility();

        // Pass 1: validate every line before touching any stock.
        record ResolvedLine(PurchaseReturnItemRequest request, PurchaseItem purchaseItem, MedicineBatch batch) {}
        List<ResolvedLine> resolved = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (PurchaseReturnItemRequest itemRequest : request.getItems()) {
            PurchaseItem purchaseItem = purchaseItemRepository.findById(itemRequest.getPurchaseItemId())
                    .filter(pi -> pi.getPurchase().getId().equals(purchase.getId()))
                    .orElseThrow(() -> new ResourceNotFoundException("GRN line not found with id: " + itemRequest.getPurchaseItemId()));

            int alreadyReturned = purchaseReturnItemRepository.sumReturnedQtyForPurchaseItem(purchaseItem.getId());
            int remaining = purchaseItem.getReceivedQty() - alreadyReturned;
            if (itemRequest.getReturnedQty() > remaining) {
                throw new InvalidDocumentStateException(
                        "Cannot return " + itemRequest.getReturnedQty() + " of " + purchaseItem.getMedicine().getName()
                                + " - only " + remaining + " remains un-returned on this GRN line");
            }

            MedicineBatch batch = medicineBatchRepository.findByStoreIdAndMedicineIdAndBatchNo(
                            purchase.getStore().getId(), purchaseItem.getMedicine().getId(), purchaseItem.getBatchNo())
                    .orElseThrow(() -> new ResourceNotFoundException("Batch not found for " + purchaseItem.getBatchNo()));
            if (batch.getQuantityOnHand() < itemRequest.getReturnedQty()) {
                throw new InsufficientStockException(
                        "Cannot return " + itemRequest.getReturnedQty() + " of " + purchaseItem.getMedicine().getName()
                                + " - only " + batch.getQuantityOnHand() + " currently on hand");
            }

            resolved.add(new ResolvedLine(itemRequest, purchaseItem, batch));
            totalAmount = totalAmount.add(purchaseItem.getRate().multiply(BigDecimal.valueOf(itemRequest.getReturnedQty())));
        }

        PurchaseReturn purchaseReturn = PurchaseReturn.builder()
                .facility(facility)
                .store(purchase.getStore())
                .returnNo(documentNumberService.next(facilityId, DOC_TYPE, PREFIX))
                .purchase(purchase)
                .supplier(purchase.getSupplier())
                .reason(request.getReason())
                .status(PurchaseReturn.STATUS_POSTED)
                .totalAmount(totalAmount)
                .createdBy(userId)
                .build();

        for (ResolvedLine line : resolved) {
            PurchaseReturnItemRequest itemRequest = line.request();
            PurchaseItem purchaseItem = line.purchaseItem();

            purchaseReturn.getItems().add(PurchaseReturnItem.builder()
                    .purchaseReturn(purchaseReturn)
                    .purchaseItem(purchaseItem)
                    .medicine(purchaseItem.getMedicine())
                    .batchNo(purchaseItem.getBatchNo())
                    .returnedQty(itemRequest.getReturnedQty())
                    .rate(purchaseItem.getRate())
                    .amount(purchaseItem.getRate().multiply(BigDecimal.valueOf(itemRequest.getReturnedQty())))
                    .build());
        }

        purchaseReturn = purchaseReturnRepository.save(purchaseReturn);

        for (ResolvedLine line : resolved) {
            PurchaseReturnItemRequest itemRequest = line.request();
            MedicineBatch batch = line.batch();
            batch.setQuantityOnHand(batch.getQuantityOnHand() - itemRequest.getReturnedQty());
            medicineBatchRepository.save(batch);

            stockLedgerEntryRepository.save(StockLedgerEntry.builder()
                    .facility(facility).store(purchase.getStore()).medicine(line.purchaseItem().getMedicine()).medicineBatch(batch)
                    .txnType(TXN_PURCHASE_RETURN).qtyIn(0).qtyOut(itemRequest.getReturnedQty()).balanceQty(batch.getQuantityOnHand())
                    .referenceType("PURCHASE_RETURN").referenceId(purchaseReturn.getId()).referenceNumber(purchaseReturn.getReturnNo())
                    .txnDate(LocalDateTime.now())
                    .build());
        }

        return PurchaseReturnResponse.toResponse(purchaseReturn);
    }

    private PurchaseReturn findEntity(Long id) {
        Long facilityId = SecurityUtils.requireFacilityId();
        return purchaseReturnRepository.findByIdAndFacilityId(id, facilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase return not found with id: " + id));
    }
}
