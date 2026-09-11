package com.lockdoc.pharmacy.service;

import com.lockdoc.common.service.DocumentNumberService;
import com.lockdoc.common.dto.PageResponse;
import com.lockdoc.pharmacy.dto.PurchaseOrderItemRequest;
import com.lockdoc.pharmacy.dto.PurchaseOrderRequest;
import com.lockdoc.pharmacy.dto.PurchaseOrderResponse;
import com.lockdoc.pharmacy.entity.Medicine;
import com.lockdoc.pharmacy.entity.PurchaseOrder;
import com.lockdoc.pharmacy.entity.PurchaseOrderItem;
import com.lockdoc.pharmacy.entity.Supplier;
import com.lockdoc.pharmacy.exception.InvalidDocumentStateException;
import com.lockdoc.common.exception.ResourceNotFoundException;
import com.lockdoc.pharmacy.repository.MedicineRepository;
import com.lockdoc.pharmacy.repository.PurchaseOrderRepository;
import com.lockdoc.pharmacy.repository.PurchaseRepository;
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
public class PurchaseOrderService {

    private static final String DOC_TYPE = "PURCHASE_ORDER";
    private static final String PREFIX = "PO";

    private static final String STATUS_DRAFT = "DRAFT";
    private static final String STATUS_APPROVED = "APPROVED";
    private static final String STATUS_PARTIALLY_RECEIVED = "PARTIALLY_RECEIVED";
    private static final String STATUS_RECEIVED = "RECEIVED";
    private static final String STATUS_CANCELLED = "CANCELLED";

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseRepository purchaseRepository;
    private final SupplierRepository supplierRepository;
    private final MedicineRepository medicineRepository;
    private final DocumentNumberService documentNumberService;

    public PageResponse<PurchaseOrderResponse> list(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PurchaseOrder> result = StringUtils.hasText(search)
                ? purchaseOrderRepository.search(search, pageable)
                : purchaseOrderRepository.findAll(pageable);
        return PageResponse.of(result, PurchaseOrderResponse::toResponse);
    }

    public PurchaseOrderResponse get(Long id) {
        return PurchaseOrderResponse.toResponse(findEntity(id));
    }

    public PurchaseOrderResponse create(PurchaseOrderRequest request, Long userId) {
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + request.getSupplierId()));

        PurchaseOrder order = PurchaseOrder.builder()
                .poNumber(documentNumberService.next(DOC_TYPE, PREFIX))
                .supplier(supplier)
                .orderDate(request.getOrderDate())
                .expectedDeliveryDate(request.getExpectedDeliveryDate())
                .status(STATUS_DRAFT)
                .remarks(request.getRemarks())
                .createdBy(userId)
                .items(new ArrayList<>())
                .build();

        List<PurchaseOrderItem> items = new ArrayList<>();
        for (PurchaseOrderItemRequest itemRequest : request.getItems()) {
            Medicine medicine = medicineRepository.findById(itemRequest.getMedicineId())
                    .orElseThrow(() -> new ResourceNotFoundException("Medicine not found with id: " + itemRequest.getMedicineId()));

            BigDecimal taxPercent = itemRequest.getTaxPercent() != null ? itemRequest.getTaxPercent() : BigDecimal.ZERO;
            BigDecimal amount = itemRequest.getRate()
                    .multiply(BigDecimal.valueOf(itemRequest.getOrderedQty()))
                    .multiply(BigDecimal.ONE.add(taxPercent.divide(BigDecimal.valueOf(100))));

            items.add(PurchaseOrderItem.builder()
                    .purchaseOrder(order)
                    .medicine(medicine)
                    .orderedQty(itemRequest.getOrderedQty())
                    .receivedQty(0)
                    .rate(itemRequest.getRate())
                    .taxPercent(taxPercent)
                    .amount(amount)
                    .build());
        }
        order.setItems(items);

        return PurchaseOrderResponse.toResponse(purchaseOrderRepository.save(order));
    }

    public PurchaseOrderResponse approve(Long id, Long userId) {
        PurchaseOrder order = findEntity(id);
        if (!STATUS_DRAFT.equals(order.getStatus())) {
            throw new InvalidDocumentStateException("Purchase order can only be approved from DRAFT status, current status: " + order.getStatus());
        }
        order.setStatus(STATUS_APPROVED);
        order.setApprovedBy(userId);
        order.setApprovedDate(LocalDateTime.now());
        return PurchaseOrderResponse.toResponse(purchaseOrderRepository.save(order));
    }

    public PurchaseOrderResponse cancel(Long id) {
        PurchaseOrder order = findEntity(id);
        if (!STATUS_DRAFT.equals(order.getStatus()) && !STATUS_APPROVED.equals(order.getStatus())) {
            throw new InvalidDocumentStateException("Purchase order cannot be cancelled from status: " + order.getStatus());
        }
        if (purchaseRepository.existsByPurchaseOrderId(order.getId())) {
            throw new InvalidDocumentStateException("Purchase order cannot be cancelled - purchases already exist against it");
        }
        order.setStatus(STATUS_CANCELLED);
        return PurchaseOrderResponse.toResponse(purchaseOrderRepository.save(order));
    }

    private PurchaseOrder findEntity(Long id) {
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found with id: " + id));
    }
}
