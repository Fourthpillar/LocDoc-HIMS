package com.lockdoc.doctor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicinesPrescribedReportResponse {
    private List<MedicinesPrescribedRow> rows;
    private int totalPrescriptions;
    private int totalLines;
    /** Each patient counted once across the whole range — per-row distinctPatients can't be summed without double-counting. */
    private int distinctPatients;
    /** HOUR, DAY, WEEK or MONTH — chosen from the range length, so a year is twelve bars, not 365. */
    private String trendGranularity;
    /** Every bucket in the range, zero-filled, oldest first. */
    private List<PrescriptionTrendBucket> trend;
}
