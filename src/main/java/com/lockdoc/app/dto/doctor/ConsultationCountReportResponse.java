package com.lockdoc.app.dto.doctor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationCountReportResponse {
    private List<ConsultationCountRow> rows;
    private int totalCount;
    private List<FacilityCount> byFacility;
    /** Sum of every row's billedAmount — the total raised across all these consultations' CONSULTATION bills, not what's actually been collected. */
    private BigDecimal totalBilled;
    private RatingSummary ratingSummary;
}
