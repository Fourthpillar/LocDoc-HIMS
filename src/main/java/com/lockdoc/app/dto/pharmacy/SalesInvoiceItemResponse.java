package com.lockdoc.app.dto.pharmacy;

import com.lockdoc.app.entity.pharmacy.SalesInvoiceItem;
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
public class SalesInvoiceItemResponse {

    private Long id;
    private Long medicineId;
    private String medicineName;
    private Long medicineBatchId;
    private String batchNo;
    private Integer qty;
    private BigDecimal rate;
    private BigDecimal taxPercent;
    private BigDecimal discountAmount;
    private BigDecimal amount;
    private Long prescriptionLineId;
    private String overrideReason;
    private String prescriberName;
    private String prescriberRegistrationNumber;

    public static SalesInvoiceItemResponse toResponse(SalesInvoiceItem i) {
        return SalesInvoiceItemResponse.builder()
                .id(i.getId())
                .medicineId(i.getMedicine().getId())
                .medicineName(i.getMedicine().getName())
                .medicineBatchId(i.getMedicineBatch().getId())
                .batchNo(i.getMedicineBatch().getBatchNo())
                .qty(i.getQty())
                .rate(i.getRate())
                .taxPercent(i.getTaxPercent())
                .discountAmount(i.getDiscountAmount())
                .amount(i.getAmount())
                .prescriptionLineId(i.getPrescriptionLineId())
                .overrideReason(i.getOverrideReason())
                .prescriberName(i.getPrescriberName())
                .prescriberRegistrationNumber(i.getPrescriberRegistrationNumber())
                .build();
    }
}
