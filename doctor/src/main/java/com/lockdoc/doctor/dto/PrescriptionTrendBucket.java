package com.lockdoc.doctor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * One bucket of the medicines report's trend. {@code start} is the bucket's
 * first moment - an hour, a day, a week or a month depending on the
 * response's trendGranularity. A first week/month bucket starts at the
 * range start rather than the calendar boundary, so it never reaches back
 * outside the dates the doctor picked.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrescriptionTrendBucket {
    private LocalDateTime start;
    private int prescriptions;
    private int lines;
}
