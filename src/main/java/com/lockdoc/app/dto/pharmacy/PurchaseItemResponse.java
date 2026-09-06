package com.lockdoc.app.dto.pharmacy;

import com.lockdoc.app.entity.pharmacy.PurchaseItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseItemResponse {

    private Long id;
    private Long medicineId;
    private String medicineName;
    private String medicineCode;
    private Long purchaseOrderItemId;
    private String batchNo;
    private LocalDate expiryDate;
    private Integer receivedQty;
    private Integer freeQty;
    private BigDecimal rate;
    private BigDecimal taxPercent;
    private BigDecimal mrp;
    private BigDecimal saleRate;
    private BigDecimal amount;

    public static PurchaseItemResponse toResponse(PurchaseItem i) {
        return PurchaseItemResponse.builder()
                .id(i.getId())
                .medicineId(i.getMedicine().getId())
                .medicineName(i.getMedicine().getName())
                .medicineCode(i.getMedicine().getCode())
                .purchaseOrderItemId(i.getPurchaseOrderItem() != null ? i.getPurchaseOrderItem().getId() : null)
                .batchNo(i.getBatchNo())
                .expiryDate(i.getExpiryDate())
                .receivedQty(i.getReceivedQty())
                .freeQty(i.getFreeQty())
                .rate(i.getRate())
                .taxPercent(i.getTaxPercent())
                .mrp(i.getMrp())
                .saleRate(i.getSaleRate())
                .amount(i.getAmount())
                .build();
    }
}
