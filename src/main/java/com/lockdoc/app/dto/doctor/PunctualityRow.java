package com.lockdoc.app.dto.doctor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PunctualityRow {
    private LocalDate date;
    private Long facilityId;
    private String facilityName;
    private LocalTime scheduledStart;
    private LocalDateTime arrivedAt;
    private Integer delayMinutes;
    /** ON_TIME | LATE | NO_STATUS | BLOCKED */
    private String outcome;
}
