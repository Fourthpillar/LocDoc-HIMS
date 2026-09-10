package com.lockdoc.pharmacy.dto;

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
public class MedicineSalesReportRow {

    private Long medicineId;
    private String medicineCode;
    private String medicineName;
    private String category;
    private String manufacturer;
    private Integer qtySold;
    private Integer qtyReturned;
    private Integer netQty;
    private BigDecimal grossAmount;
    private BigDecimal discountAmount;
    private BigDecimal returnedAmount;
    private BigDecimal netAmount;
}
