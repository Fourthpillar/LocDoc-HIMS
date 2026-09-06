package com.lockdoc.app.dto.pharmacy;

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
public class StockDetailReportRow {

    private Long medicineId;
    private String medicineCode;
    private String medicineName;
    private String category;
    private String manufacturer;
    private String batchNo;
    private LocalDate expiryDate;
    private Long supplierId;
    private String supplierName;
    private Integer quantityOnHand;
    private BigDecimal purchaseRate;
    private BigDecimal saleRate;
    private BigDecimal mrp;
    private BigDecimal stockValueAtCost;
    private BigDecimal stockValueAtMrp;
}
