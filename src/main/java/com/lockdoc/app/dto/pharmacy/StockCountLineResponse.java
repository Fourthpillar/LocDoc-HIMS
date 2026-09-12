package com.lockdoc.app.dto.pharmacy;

import com.lockdoc.app.entity.pharmacy.StockCountLine;
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
public class StockCountLineResponse {

    private Long id;
    private Long medicineId;
    private String medicineName;
    private Long medicineBatchId;
    private String batchNo;
    /** Only populated once the count is APPROVED - "blind" until then (§11.4). */
    private Integer systemQty;
    private Integer countedQty;
    private Integer variance;

    public static StockCountLineResponse toResponse(StockCountLine l, boolean revealSystemQty) {
        return StockCountLineResponse.builder()
                .id(l.getId())
                .medicineId(l.getMedicine().getId())
                .medicineName(l.getMedicine().getName())
                .medicineBatchId(l.getMedicineBatch().getId())
                .batchNo(l.getMedicineBatch().getBatchNo())
                .systemQty(revealSystemQty ? l.getSystemQty() : null)
                .countedQty(l.getCountedQty())
                .variance(revealSystemQty ? l.getVariance() : null)
                .build();
    }
}
