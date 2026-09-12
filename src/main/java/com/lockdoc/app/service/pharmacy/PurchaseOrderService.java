package com.lockdoc.app.service.pharmacy;

import com.lockdoc.app.config.SecurityUtils;
import com.lockdoc.app.dto.PageResponse;
import com.lockdoc.app.dto.pharmacy.PurchaseOrderItemRequest;
import com.lockdoc.app.dto.pharmacy.PurchaseOrderRequest;
import com.lockdoc.app.dto.pharmacy.PurchaseOrderResponse;
import com.lockdoc.app.entity.Facility;
import com.lockdoc.app.entity.Store;
import com.lockdoc.app.entity.pharmacy.Medicine;
import com.lockdoc.app.entity.pharmacy.PurchaseOrder;
import com.lockdoc.app.entity.pharmacy.PurchaseOrderItem;
import com.lockdoc.app.entity.pharmacy.Supplier;
import com.lockdoc.app.exception.InvalidDocumentStateException;
import com.lockdoc.app.exception.ResourceNotFoundException;
import com.lockdoc.app.repository.FacilityRepository;
import com.lockdoc.app.repository.pharmacy.MedicineRepository;
import com.lockdoc.app.repository.pharmacy.PurchaseOrderRepository;
import com.lockdoc.app.repository.pharmacy.PurchaseRepository;
import com.lockdoc.app.repository.pharmacy.SupplierRepository;
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
    private final FacilityRepository facilityRepository;
    private final StoreResolutionService storeResolutionService;
    private final DocumentNumberService documentNumberService;

    public PageResponse<PurchaseOrderResponse> list(int page, int size, String search) {
        Long facilityId = SecurityUtils.requireFacilityId();
        Pageable pageable = PageRequest.of(page, size);
        Page<PurchaseOrder> result = StringUtils.hasText(search)
                ? purchaseOrderRepository.search(facilityId, search, pageable)
                : purchaseOrderRepository.findByFacilityId(facilityId, pageable);
        return PageResponse.of(result, PurchaseOrderResponse::toResponse);
    }

    public PurchaseOrderResponse get(Long id) {
        return PurchaseOrderResponse.toResponse(findEntity(id));
    }

    public PurchaseOrderResponse create(PurchaseOrderRequest request, Long userId) {
        Long facilityId = SecurityUtils.requireFacilityId();
        Facility facility = facilityRepository.getReferenceById(facilityId);
        Store store = storeResolutionService.resolveDefaultStore(facilityId);

        Supplier supplier = supplierRepository.findByIdAndFacilityId(request.getSupplierId(), facilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + request.getSupplierId()));
        if (!Boolean.TRUE.equals(supplier.getAccepted())) {
            throw new InvalidDocumentStateException(
                    "Supplier '" + supplier.getName() + "' has not yet been accepted by the Hospital/Clinic Admin - see Supplier Acceptance (Master Spec §12)");
        }

        PurchaseOrder order = PurchaseOrder.builder()
                .facility(facility)
                .store(store)
                .poNumber(documentNumberService.next(facilityId, DOC_TYPE, PREFIX))
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
            Medicine medicine = medicineRepository.findByIdAndFacilityId(itemRequest.getMedicineId(), facilityId)
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
        if (purchaseRepository.existsByFacilityIdAndPurchaseOrderId(order.getFacility().getId(), order.getId())) {
            throw new InvalidDocumentStateException("Purchase order cannot be cancelled - purchases already exist against it");
        }
        order.setStatus(STATUS_CANCELLED);
        return PurchaseOrderResponse.toResponse(purchaseOrderRepository.save(order));
    }

    private PurchaseOrder findEntity(Long id) {
        Long facilityId = SecurityUtils.requireFacilityId();
        return purchaseOrderRepository.findByIdAndFacilityId(id, facilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found with id: " + id));
    }
}
