package com.lockdoc.app.dto.pharmacy;

import com.lockdoc.app.entity.pharmacy.PurchaseReturn;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseReturnResponse {

    private Long id;
    private String returnNo;
    private Long purchaseId;
    private String purchaseGrnNumber;
    private Long supplierId;
    private String supplierName;
    private String reason;
    private String status;
    private BigDecimal totalAmount;
    private List<PurchaseReturnItemResponse> items;
    private LocalDateTime createdDate;

    public static PurchaseReturnResponse toResponse(PurchaseReturn r) {
        return PurchaseReturnResponse.builder()
                .id(r.getId())
                .returnNo(r.getReturnNo())
                .purchaseId(r.getPurchase().getId())
                .purchaseGrnNumber(r.getPurchase().getGrnNumber())
                .supplierId(r.getSupplier().getId())
                .supplierName(r.getSupplier().getName())
                .reason(r.getReason())
                .status(r.getStatus())
                .totalAmount(r.getTotalAmount())
                .items(r.getItems().stream().map(PurchaseReturnItemResponse::toResponse).toList())
                .createdDate(r.getCreatedDate())
                .build();
    }
}
