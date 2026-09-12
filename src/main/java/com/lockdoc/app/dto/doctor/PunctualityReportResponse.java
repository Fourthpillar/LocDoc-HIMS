package com.lockdoc.app.dto.doctor;

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
public class PunctualityReportResponse {
    private List<PunctualityRow> rows;
    private int sessionsWithStatus;
    private int onTimeCount;
    private int lateCount;
    private int noStatusCount;
    private int blockedCount;
    private Double avgDelayMinutes;
    /** Same patient-satisfaction rollup as the Consultation tab, over this same date range/facility — punctuality and quality read together, not as two unrelated screens. */
    private RatingSummary ratingSummary;
}
