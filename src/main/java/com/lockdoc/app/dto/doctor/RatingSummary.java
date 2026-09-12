package com.lockdoc.app.dto.doctor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

/**
 * Patient-satisfaction rollup for a date range (Master Spec §17.7 #18) -
 * shared by both the Punctuality and Consultation report tabs so a
 * doctor's quality signal sits alongside their timeliness and volume
 * signals, not buried in a third place. {@code totalEligible} is every
 * completed consultation in range, rated or not, so the UI can show
 * "N of M consultations rated" rather than implying every visit gets one.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RatingSummary {
    /** Null when nothing has been rated yet in this range. */
    private Double average;
    private Integer ratedCount;
    private Integer totalEligible;
    /** Star (1-5) -> count, always all 5 keys present even at zero. */
    private Map<Integer, Integer> distribution;
}
