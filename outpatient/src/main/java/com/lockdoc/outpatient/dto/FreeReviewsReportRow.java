package com.lockdoc.outpatient.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** One free-review visit - "Free review — 2nd of 3, against consultation OP/2026/01432" (§7.4's own example). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FreeReviewsReportRow {
    private String patientName;
    private String patientMrn;
    private String doctorName;
    private LocalDateTime visitedAt;
    private String originalOpNo;
    private int position;
    private int maxVisits;
}
