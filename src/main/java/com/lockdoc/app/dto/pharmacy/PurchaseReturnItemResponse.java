package com.lockdoc.app.dto.pharmacy;

import com.lockdoc.app.entity.pharmacy.PurchaseReturnItem;
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
public class PurchaseReturnItemResponse {

    private Long id;
    private Long purchaseItemId;
    private Long medicineId;
    private String medicineName;
    private String batchNo;
    private Integer returnedQty;
    private BigDecimal rate;
    private BigDecimal amount;

    public static PurchaseReturnItemResponse toResponse(PurchaseReturnItem i) {
        return PurchaseReturnItemResponse.builder()
                .id(i.getId())
                .purchaseItemId(i.getPurchaseItem().getId())
                .medicineId(i.getMedicine().getId())
                .medicineName(i.getMedicine().getName())
                .batchNo(i.getBatchNo())
                .returnedQty(i.getReturnedQty())
                .rate(i.getRate())
                .amount(i.getAmount())
                .build();
    }
}
