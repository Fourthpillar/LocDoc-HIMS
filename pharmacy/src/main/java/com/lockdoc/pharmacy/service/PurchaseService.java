package com.lockdoc.pharmacy.service;

import com.lockdoc.common.service.DocumentNumberService;
import com.lockdoc.common.dto.PageResponse;
import com.lockdoc.pharmacy.dto.PurchaseItemRequest;
import com.lockdoc.pharmacy.dto.PurchaseRequest;
import com.lockdoc.pharmacy.dto.PurchaseResponse;
import com.lockdoc.pharmacy.entity.Medicine;
import com.lockdoc.pharmacy.entity.MedicineBatch;
import com.lockdoc.pharmacy.entity.Purchase;
import com.lockdoc.pharmacy.entity.PurchaseItem;
import com.lockdoc.pharmacy.entity.PurchaseOrder;
import com.lockdoc.pharmacy.entity.PurchaseOrderItem;
import com.lockdoc.pharmacy.entity.StockLedgerEntry;
import com.lockdoc.pharmacy.entity.Supplier;
import com.lockdoc.pharmacy.exception.InvalidDocumentStateException;
import com.lockdoc.common.exception.ResourceNotFoundException;
import com.lockdoc.pharmacy.repository.MedicineBatchRepository;
import com.lockdoc.pharmacy.repository.MedicineRepository;
import com.lockdoc.pharmacy.repository.PurchaseOrderItemRepository;
import com.lockdoc.pharmacy.repository.PurchaseOrderRepository;
import com.lockdoc.pharmacy.repository.PurchaseRepository;
import com.lockdoc.pharmacy.repository.StockLedgerEntryRepository;
import com.lockdoc.pharmacy.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PurchaseService {

    private static final String DOC_TYPE = "PURCHASE";
    private static final String PREFIX = "GRN";

    private static final String STATUS_POSTED = "POSTED";
    private static final String STATUS_CANCELLED = "CANCELLED";

    private static final String PO_STATUS_APPROVED = "APPROVED";
    private static final String PO_STATUS_PARTIALLY_RECEIVED = "PARTIALLY_RECEIVED";
    private static final String PO_STATUS_RECEIVED = "RECEIVED";

    private static final String TXN_TYPE_PURCHASE = "PURCHASE";
    private static final String REFERENCE_TYPE_PURCHASE = "PURCHASE";

    private final PurchaseRepository purchaseRepository;
    private final SupplierRepository supplierRepository;
    private final MedicineRepository medicineRepository;
    private final MedicineBatchRepository medicineBatchRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final StockLedgerEntryRepository stockLedgerEntryRepository;
    private final DocumentNumberService documentNumberService;

    public PageResponse<PurchaseResponse> list(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Purchase> result = StringUtils.hasText(search)
                ? purchaseRepository.search(search, pageable)
                : purchaseRepository.findAll(pageable);
        return PageResponse.of(result, PurchaseResponse::toResponse);
    }

    public PurchaseResponse get(Long id) {
        return PurchaseResponse.toResponse(findEntity(id));
    }

    public PurchaseResponse create(PurchaseRequest request, Long userId) {
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + request.getSupplierId()));

        PurchaseOrder purchaseOrder = null;
        if (request.getPurchaseOrderId() != null) {
            purchaseOrder = purchaseOrderRepository.findById(request.getPurchaseOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found with id: " + request.getPurchaseOrderId()));
            if (!PO_STATUS_APPROVED.equals(purchaseOrder.getStatus()) && !PO_STATUS_PARTIALLY_RECEIVED.equals(purchaseOrder.getStatus())) {
                throw new InvalidDocumentStateException(
                        "Purchase order must be APPROVED or PARTIALLY_RECEIVED to receive against it, current status: " + purchaseOrder.getStatus());
            }
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<PurchaseItem> items = new ArrayList<>();
        for (PurchaseItemRequest itemRequest : request.getItems()) {
            totalAmount = totalAmount.add(computeAmount(itemRequest));
        }

        BigDecimal amountPaid = request.getAmountPaid() != null ? request.getAmountPaid() : BigDecimal.ZERO;
        BigDecimal balanceDue = totalAmount.subtract(amountPaid);

        Purchase purchase = Purchase.builder()
                .grnNumber(documentNumberService.next(DOC_TYPE, PREFIX))
                .purchaseOrder(purchaseOrder)
                .supplier(supplier)
                .purchaseDate(request.getPurchaseDate())
                .supplierInvoiceNumber(request.getSupplierInvoiceNumber())
                .supplierInvoiceDate(request.getSupplierInvoiceDate())
                .status(STATUS_POSTED)
                .remarks(request.getRemarks())
                .totalAmount(totalAmount)
                .amountPaid(amountPaid)
                .balanceDue(balanceDue)
                .dueDate(request.getDueDate())
                .createdBy(userId)
                .items(new ArrayList<>())
                .build();

        for (PurchaseItemRequest itemRequest : request.getItems()) {
            Medicine medicine = medicineRepository.findById(itemRequest.getMedicineId())
                    .orElseThrow(() -> new ResourceNotFoundException("Medicine not found with id: " + itemRequest.getMedicineId()));

            PurchaseOrderItem poItem = null;
            if (itemRequest.getPurchaseOrderItemId() != null) {
                poItem = purchaseOrderItemRepository.findById(itemRequest.getPurchaseOrderItemId())
                        .orElseThrow(() -> new ResourceNotFoundException("Purchase order item not found with id: " + itemRequest.getPurchaseOrderItemId()));
            }

            items.add(PurchaseItem.builder()
                    .purchase(purchase)
                    .purchaseOrderItem(poItem)
                    .medicine(medicine)
                    .batchNo(itemRequest.getBatchNo())
                    .expiryDate(itemRequest.getExpiryDate())
                    .receivedQty(itemRequest.getReceivedQty())
                    .freeQty(itemRequest.getFreeQty() != null ? itemRequest.getFreeQty() : 0)
                    .rate(itemRequest.getRate())
                    .taxPercent(itemRequest.getTaxPercent() != null ? itemRequest.getTaxPercent() : BigDecimal.ZERO)
                    .mrp(itemRequest.getMrp())
                    .saleRate(itemRequest.getSaleRate())
                    .amount(computeAmount(itemRequest))
                    .build());
        }
        purchase.setItems(items);

        // IDENTITY generation flushes inserts immediately, so ids are populated after save
        purchase = purchaseRepository.save(purchase);

        for (PurchaseItem item : purchase.getItems()) {
            MedicineBatch batch = upsertBatch(item, supplier);

            stockLedgerEntryRepository.save(StockLedgerEntry.builder()
                    .medicine(item.getMedicine())
                    .medicineBatch(batch)
                    .txnType(TXN_TYPE_PURCHASE)
                    .qtyIn(item.getReceivedQty())
                    .qtyOut(0)
                    .balanceQty(batch.getQuantityOnHand())
                    .referenceType(REFERENCE_TYPE_PURCHASE)
                    .referenceId(purchase.getId())
                    .referenceNumber(purchase.getGrnNumber())
                    .txnDate(LocalDateTime.now())
                    .build());

            if (item.getPurchaseOrderItem() != null) {
                PurchaseOrderItem poItem = item.getPurchaseOrderItem();
                poItem.setReceivedQty(poItem.getReceivedQty() + item.getReceivedQty());
                purchaseOrderItemRepository.save(poItem);
            }
        }

        if (purchaseOrder != null) {
            recomputePurchaseOrderStatus(purchaseOrder);
        }

        return PurchaseResponse.toResponse(purchase);
    }

    public PurchaseResponse cancel(Long id) {
        Purchase purchase = findEntity(id);
        if (!STATUS_POSTED.equals(purchase.getStatus())) {
            throw new InvalidDocumentStateException("Purchase cannot be cancelled from status: " + purchase.getStatus());
        }

        // Validate ALL lines first so a mid-way failure never leaves a partial reversal
        for (PurchaseItem item : purchase.getItems()) {
            MedicineBatch batch = medicineBatchRepository.findByMedicineIdAndBatchNo(item.getMedicine().getId(), item.getBatchNo())
                    .orElseThrow(() -> new ResourceNotFoundException("Batch not found for medicine/batch: " + item.getBatchNo()));
            if (batch.getQuantityOnHand() < item.getReceivedQty()) {
                throw new InvalidDocumentStateException(
                        "Cannot cancel purchase " + purchase.getGrnNumber() + " - batch " + batch.getBatchNo()
                                + " stock has already been consumed below the received quantity");
            }
        }

        for (PurchaseItem item : purchase.getItems()) {
            MedicineBatch batch = medicineBatchRepository.findByMedicineIdAndBatchNo(item.getMedicine().getId(), item.getBatchNo())
                    .orElseThrow(() -> new ResourceNotFoundException("Batch not found for medicine/batch: " + item.getBatchNo()));

            batch.setQuantityOnHand(batch.getQuantityOnHand() - item.getReceivedQty());
            medicineBatchRepository.save(batch);

            stockLedgerEntryRepository.save(StockLedgerEntry.builder()
                    .medicine(item.getMedicine())
                    .medicineBatch(batch)
                    .txnType(TXN_TYPE_PURCHASE)
                    .qtyIn(0)
                    .qtyOut(item.getReceivedQty())
                    .balanceQty(batch.getQuantityOnHand())
                    .referenceType(REFERENCE_TYPE_PURCHASE)
                    .referenceId(purchase.getId())
                    .referenceNumber(purchase.getGrnNumber())
                    .txnDate(LocalDateTime.now())
                    .build());

            if (item.getPurchaseOrderItem() != null) {
                PurchaseOrderItem poItem = item.getPurchaseOrderItem();
                poItem.setReceivedQty(Math.max(0, poItem.getReceivedQty() - item.getReceivedQty()));
                purchaseOrderItemRepository.save(poItem);
            }
        }

        purchase.setStatus(STATUS_CANCELLED);
        purchase = purchaseRepository.save(purchase);

        if (purchase.getPurchaseOrder() != null) {
            recomputePurchaseOrderStatus(purchase.getPurchaseOrder());
        }

        return PurchaseResponse.toResponse(purchase);
    }

    private MedicineBatch upsertBatch(PurchaseItem item, Supplier supplier) {
        MedicineBatch batch = medicineBatchRepository.findByMedicineIdAndBatchNo(item.getMedicine().getId(), item.getBatchNo())
                .orElse(null);

        if (batch == null) {
            batch = MedicineBatch.builder()
                    .medicine(item.getMedicine())
                    .batchNo(item.getBatchNo())
                    .expiryDate(item.getExpiryDate())
                    .mrp(item.getMrp())
                    .purchaseRate(item.getRate())
                    .saleRate(item.getSaleRate())
                    .quantityOnHand(item.getReceivedQty())
                    .supplier(supplier)
                    .sourcePurchaseItemId(item.getId())
                    .build();
        } else {
            batch.setQuantityOnHand(batch.getQuantityOnHand() + item.getReceivedQty());
            batch.setMrp(item.getMrp());
            batch.setPurchaseRate(item.getRate());
            batch.setSaleRate(item.getSaleRate());
            batch.setExpiryDate(item.getExpiryDate());
        }

        return medicineBatchRepository.save(batch);
    }

    private void recomputePurchaseOrderStatus(PurchaseOrder purchaseOrder) {
        List<PurchaseOrderItem> items = purchaseOrderItemRepository.findByPurchaseOrderId(purchaseOrder.getId());
        boolean allReceived = items.stream().allMatch(i -> i.getReceivedQty() >= i.getOrderedQty());
        boolean anyReceived = items.stream().anyMatch(i -> i.getReceivedQty() > 0);

        String newStatus;
        if (allReceived) {
            newStatus = PO_STATUS_RECEIVED;
        } else if (anyReceived) {
            newStatus = PO_STATUS_PARTIALLY_RECEIVED;
        } else {
            newStatus = PO_STATUS_APPROVED;
        }

        purchaseOrder.setStatus(newStatus);
        purchaseOrderRepository.save(purchaseOrder);
    }

    private BigDecimal computeAmount(PurchaseItemRequest itemRequest) {
        BigDecimal taxPercent = itemRequest.getTaxPercent() != null ? itemRequest.getTaxPercent() : BigDecimal.ZERO;
        return itemRequest.getRate()
                .multiply(BigDecimal.valueOf(itemRequest.getReceivedQty()))
                .multiply(BigDecimal.ONE.add(taxPercent.divide(BigDecimal.valueOf(100))));
    }

    private Purchase findEntity(Long id) {
        return purchaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase not found with id: " + id));
    }
}
