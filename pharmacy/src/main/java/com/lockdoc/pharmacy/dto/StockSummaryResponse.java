package com.lockdoc.pharmacy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockSummaryResponse {

    private Long medicineId;
    private String medicineCode;
    private String medicineName;
    private String uom;
    private Integer reorderLevel;
    private Integer stockOnHand;
    private Boolean lowStock;
}
