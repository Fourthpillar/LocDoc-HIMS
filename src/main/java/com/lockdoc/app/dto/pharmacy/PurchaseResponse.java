package com.lockdoc.app.dto.pharmacy;

import com.lockdoc.app.entity.pharmacy.Purchase;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseResponse {

    private Long id;
    private String grnNumber;
    private Long purchaseOrderId;
    private String poNumber;
    private Long supplierId;
    private String supplierName;
    private LocalDate purchaseDate;
    private String supplierInvoiceNumber;
    private LocalDate supplierInvoiceDate;
    private String status;
    private String remarks;
    private BigDecimal totalAmount;
    private BigDecimal amountPaid;
    private BigDecimal balanceDue;
    private LocalDate dueDate;
    private Long createdBy;
    private List<PurchaseItemResponse> items;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    /** Non-blocking - batches received within the near-expiry window (§11.3's "expiry guard"), informational only. */
    private List<String> nearExpiryWarnings;

    public static PurchaseResponse toResponse(Purchase p) {
        return toResponse(p, java.util.List.of());
    }

    public static PurchaseResponse toResponse(Purchase p, List<String> nearExpiryWarnings) {
        return PurchaseResponse.builder()
                .id(p.getId())
                .grnNumber(p.getGrnNumber())
                .purchaseOrderId(p.getPurchaseOrder() != null ? p.getPurchaseOrder().getId() : null)
                .poNumber(p.getPurchaseOrder() != null ? p.getPurchaseOrder().getPoNumber() : null)
                .supplierId(p.getSupplier().getId())
                .supplierName(p.getSupplier().getName())
                .purchaseDate(p.getPurchaseDate())
                .supplierInvoiceNumber(p.getSupplierInvoiceNumber())
                .supplierInvoiceDate(p.getSupplierInvoiceDate())
                .status(p.getStatus())
                .remarks(p.getRemarks())
                .totalAmount(p.getTotalAmount())
                .amountPaid(p.getAmountPaid())
                .balanceDue(p.getBalanceDue())
                .dueDate(p.getDueDate())
                .createdBy(p.getCreatedBy())
                .items(p.getItems().stream().map(PurchaseItemResponse::toResponse).toList())
                .createdDate(p.getCreatedDate())
                .updatedDate(p.getUpdatedDate())
                .nearExpiryWarnings(nearExpiryWarnings)
                .build();
    }
}
