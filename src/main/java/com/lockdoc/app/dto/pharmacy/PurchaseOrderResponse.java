package com.lockdoc.app.dto.pharmacy;

import com.lockdoc.app.entity.pharmacy.PurchaseOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseOrderResponse {

    private Long id;
    private String poNumber;
    private Long supplierId;
    private String supplierName;
    private LocalDate orderDate;
    private LocalDate expectedDeliveryDate;
    private String status;
    private String remarks;
    private Long createdBy;
    private Long approvedBy;
    private LocalDateTime approvedDate;
    private List<PurchaseOrderItemResponse> items;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    public static PurchaseOrderResponse toResponse(PurchaseOrder po) {
        return PurchaseOrderResponse.builder()
                .id(po.getId())
                .poNumber(po.getPoNumber())
                .supplierId(po.getSupplier().getId())
                .supplierName(po.getSupplier().getName())
                .orderDate(po.getOrderDate())
                .expectedDeliveryDate(po.getExpectedDeliveryDate())
                .status(po.getStatus())
                .remarks(po.getRemarks())
                .createdBy(po.getCreatedBy())
                .approvedBy(po.getApprovedBy())
                .approvedDate(po.getApprovedDate())
                .items(po.getItems().stream().map(PurchaseOrderItemResponse::toResponse).toList())
                .createdDate(po.getCreatedDate())
                .updatedDate(po.getUpdatedDate())
                .build();
    }
}
