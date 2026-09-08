package com.lockdoc.app.dto.pharmacy;

import com.lockdoc.app.entity.pharmacy.SalesReturnItem;
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
public class SalesReturnItemResponse {

    private Long id;
    private Long salesInvoiceItemId;
    private Long medicineId;
    private String medicineName;
    private Long medicineBatchId;
    private String batchNo;
    private Integer qty;
    private BigDecimal rate;
    private BigDecimal amount;

    public static SalesReturnItemResponse toResponse(SalesReturnItem i) {
        return SalesReturnItemResponse.builder()
                .id(i.getId())
                .salesInvoiceItemId(i.getSalesInvoiceItem().getId())
                .medicineId(i.getMedicine().getId())
                .medicineName(i.getMedicine().getName())
                .medicineBatchId(i.getMedicineBatch().getId())
                .batchNo(i.getMedicineBatch().getBatchNo())
                .qty(i.getQty())
                .rate(i.getRate())
                .amount(i.getAmount())
                .build();
    }
}
