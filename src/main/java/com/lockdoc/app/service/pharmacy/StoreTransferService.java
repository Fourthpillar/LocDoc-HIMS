package com.lockdoc.app.service.pharmacy;

import com.lockdoc.app.config.SecurityUtils;
import com.lockdoc.app.dto.PageResponse;
import com.lockdoc.app.dto.pharmacy.*;
import com.lockdoc.app.entity.Facility;
import com.lockdoc.app.entity.Store;
import com.lockdoc.app.entity.pharmacy.Medicine;
import com.lockdoc.app.entity.pharmacy.MedicineBatch;
import com.lockdoc.app.entity.pharmacy.StockLedgerEntry;
import com.lockdoc.app.entity.pharmacy.StoreTransfer;
import com.lockdoc.app.entity.pharmacy.StoreTransferItem;
import com.lockdoc.app.exception.InsufficientStockException;
import com.lockdoc.app.exception.InvalidDocumentStateException;
import com.lockdoc.app.exception.ResourceNotFoundException;
import com.lockdoc.app.repository.StoreRepository;
import com.lockdoc.app.repository.pharmacy.MedicineBatchRepository;
import com.lockdoc.app.repository.pharmacy.MedicineRepository;
import com.lockdoc.app.repository.pharmacy.StockLedgerEntryRepository;
import com.lockdoc.app.repository.pharmacy.StoreTransferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Inter-store movement (Master Spec §6/§11.4) - issue -> in-transit ->
 * receipt, with discrepancy captured on receipt. Unlike Indent's FEFO
 * auto-selection, a transfer names the exact batch to move (the
 * pharmacist is standing in front of it) - simpler by design.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class StoreTransferService {

    private static final String DOC_TYPE = "STORE_TRANSFER";
    private static final String PREFIX = "TRF";
    private static final String TXN_TRANSFER_OUT = "TRANSFER_OUT";
    private static final String TXN_TRANSFER_IN = "TRANSFER_IN";

    private final StoreTransferRepository storeTransferRepository;
    private final StoreRepository storeRepository;
    private final MedicineRepository medicineRepository;
    private final MedicineBatchRepository medicineBatchRepository;
    private final StockLedgerEntryRepository stockLedgerEntryRepository;
    private final DocumentNumberService documentNumberService;

    public PageResponse<StoreTransferResponse> list(int page, int size) {
        Long facilityId = SecurityUtils.requireFacilityId();
        Page<StoreTransfer> result = storeTransferRepository.findByFacilityId(facilityId, PageRequest.of(page, size));
        return PageResponse.of(result, StoreTransferResponse::toResponse);
    }

    public StoreTransferResponse get(Long id) {
        return StoreTransferResponse.toResponse(findEntity(id));
    }

    /** Issuing happens immediately at creation - stock leaves the source store the moment the transfer is raised. */
    public StoreTransferResponse create(StoreTransferRequest request, Long userId) {
        Long facilityId = SecurityUtils.requireFacilityId();

        Store fromStore = storeRepository.findById(request.getFromStoreId())
                .filter(s -> s.getFacility().getId().equals(facilityId))
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with id: " + request.getFromStoreId()));
        Store toStore = storeRepository.findById(request.getToStoreId())
                .filter(s -> s.getFacility().getId().equals(facilityId))
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with id: " + request.getToStoreId()));
        if (fromStore.getId().equals(toStore.getId())) {
            throw new InvalidDocumentStateException("fromStoreId and toStoreId cannot be the same store");
        }
        Facility facility = fromStore.getFacility();

        // Pass 1: resolve + validate every line before moving any stock.
        record ResolvedLine(StoreTransferItemRequest request, Medicine medicine, MedicineBatch batch) {}
        var resolved = new java.util.ArrayList<ResolvedLine>();
        for (StoreTransferItemRequest itemRequest : request.getItems()) {
            Medicine medicine = medicineRepository.findByIdAndFacilityId(itemRequest.getMedicineId(), facilityId)
                    .orElseThrow(() -> new ResourceNotFoundException("Medicine not found with id: " + itemRequest.getMedicineId()));
            MedicineBatch batch = medicineBatchRepository.findByStoreIdAndMedicineIdAndBatchNo(fromStore.getId(), medicine.getId(), itemRequest.getBatchNo())
                    .orElseThrow(() -> new ResourceNotFoundException("Batch " + itemRequest.getBatchNo() + " not found at " + fromStore.getName()));
            if (batch.getQuantityOnHand() < itemRequest.getIssuedQty()) {
                throw new InsufficientStockException(
                        "Insufficient stock at " + fromStore.getName() + " for " + medicine.getName()
                                + " batch " + itemRequest.getBatchNo() + " - available: " + batch.getQuantityOnHand() + ", requested: " + itemRequest.getIssuedQty());
            }
            resolved.add(new ResolvedLine(itemRequest, medicine, batch));
        }

        StoreTransfer transfer = StoreTransfer.builder()
                .facility(facility).transferNo(documentNumberService.next(facilityId, DOC_TYPE, PREFIX))
                .fromStore(fromStore).toStore(toStore).status(StoreTransfer.STATUS_IN_TRANSIT)
                .issuedByUserId(userId).issuedDate(LocalDateTime.now())
                .build();

        for (ResolvedLine line : resolved) {
            transfer.getItems().add(StoreTransferItem.builder()
                    .storeTransfer(transfer).medicine(line.medicine()).batchNo(line.request().getBatchNo())
                    .expiryDate(line.batch().getExpiryDate()).issuedQty(line.request().getIssuedQty())
                    .build());
        }
        transfer = storeTransferRepository.save(transfer);

        for (ResolvedLine line : resolved) {
            MedicineBatch batch = line.batch();
            batch.setQuantityOnHand(batch.getQuantityOnHand() - line.request().getIssuedQty());
            medicineBatchRepository.save(batch);

            stockLedgerEntryRepository.save(StockLedgerEntry.builder()
                    .facility(facility).store(fromStore).medicine(line.medicine()).medicineBatch(batch)
                    .txnType(TXN_TRANSFER_OUT).qtyIn(0).qtyOut(line.request().getIssuedQty()).balanceQty(batch.getQuantityOnHand())
                    .referenceType("STORE_TRANSFER").referenceId(transfer.getId()).referenceNumber(transfer.getTransferNo())
                    .txnDate(LocalDateTime.now())
                    .build());
        }

        return StoreTransferResponse.toResponse(transfer);
    }

    /** Receipt at the destination - what actually arrived may be less than what was issued; the gap is the discrepancy (§11.4). */
    public StoreTransferResponse receive(Long id, StoreTransferReceiveRequest request, Long userId) {
        StoreTransfer transfer = findEntity(id);
        if (!StoreTransfer.STATUS_IN_TRANSIT.equals(transfer.getStatus())) {
            throw new InvalidDocumentStateException("Transfer can only be received from IN_TRANSIT status, current status: " + transfer.getStatus());
        }

        Map<Long, Integer> receivedByLine = new HashMap<>();
        for (StoreTransferReceiveItemRequest itemRequest : request.getItems()) {
            receivedByLine.put(itemRequest.getLineId(), itemRequest.getReceivedQty());
        }

        for (StoreTransferItem item : transfer.getItems()) {
            int receivedQty = receivedByLine.getOrDefault(item.getId(), item.getIssuedQty());
            if (receivedQty > item.getIssuedQty()) {
                throw new InvalidDocumentStateException("Received quantity cannot exceed issued quantity for " + item.getMedicine().getName());
            }
            item.setReceivedQty(receivedQty);

            if (receivedQty > 0) {
                MedicineBatch destBatch = medicineBatchRepository.findByStoreIdAndMedicineIdAndBatchNo(
                                transfer.getToStore().getId(), item.getMedicine().getId(), item.getBatchNo())
                        .orElseGet(() -> {
                            MedicineBatch sourceBatch = medicineBatchRepository.findByStoreIdAndMedicineIdAndBatchNo(
                                    transfer.getFromStore().getId(), item.getMedicine().getId(), item.getBatchNo()).orElse(null);
                            return MedicineBatch.builder()
                                    .facility(transfer.getFacility()).store(transfer.getToStore()).medicine(item.getMedicine())
                                    .batchNo(item.getBatchNo()).expiryDate(item.getExpiryDate())
                                    .mrp(sourceBatch != null ? sourceBatch.getMrp() : java.math.BigDecimal.ZERO)
                                    .purchaseRate(sourceBatch != null ? sourceBatch.getPurchaseRate() : java.math.BigDecimal.ZERO)
                                    .saleRate(sourceBatch != null ? sourceBatch.getSaleRate() : java.math.BigDecimal.ZERO)
                                    .quantityOnHand(0)
                                    .build();
                        });
                destBatch.setQuantityOnHand(destBatch.getQuantityOnHand() + receivedQty);
                destBatch = medicineBatchRepository.save(destBatch);

                stockLedgerEntryRepository.save(StockLedgerEntry.builder()
                        .facility(transfer.getFacility()).store(transfer.getToStore()).medicine(item.getMedicine()).medicineBatch(destBatch)
                        .txnType(TXN_TRANSFER_IN).qtyIn(receivedQty).qtyOut(0).balanceQty(destBatch.getQuantityOnHand())
                        .referenceType("STORE_TRANSFER").referenceId(transfer.getId()).referenceNumber(transfer.getTransferNo())
                        .txnDate(LocalDateTime.now())
                        .build());
            }
        }

        transfer.setStatus(StoreTransfer.STATUS_RECEIVED);
        transfer.setReceivedByUserId(userId);
        transfer.setReceivedDate(LocalDateTime.now());
        transfer.setDiscrepancyNotes(request.getDiscrepancyNotes());

        return StoreTransferResponse.toResponse(storeTransferRepository.save(transfer));
    }

    private StoreTransfer findEntity(Long id) {
        Long facilityId = SecurityUtils.requireFacilityId();
        return storeTransferRepository.findByIdAndFacilityId(id, facilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Store transfer not found with id: " + id));
    }
}
