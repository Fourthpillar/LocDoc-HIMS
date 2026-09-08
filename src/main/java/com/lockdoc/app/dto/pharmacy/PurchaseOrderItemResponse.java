package com.lockdoc.app.dto.pharmacy;

import com.lockdoc.app.entity.pharmacy.PurchaseOrderItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseOrderItemResponse {

    private Long id;
    private Long medicineId;
    private String medicineName;
    private String medicineCode;
    private Integer orderedQty;
    private Integer receivedQty;
    private BigDecimal rate;
    private BigDecimal taxPercent;
    private BigDecimal amount;

    public static PurchaseOrderItemResponse toResponse(PurchaseOrderItem i) {
        return PurchaseOrderItemResponse.builder()
                .id(i.getId())
                .medicineId(i.getMedicine().getId())
                .medicineName(i.getMedicine().getName())
                .medicineCode(i.getMedicine().getCode())
                .orderedQty(i.getOrderedQty())
                .receivedQty(i.getReceivedQty())
                .rate(i.getRate())
                .taxPercent(i.getTaxPercent())
                .amount(i.getAmount())
                .build();
    }
}
