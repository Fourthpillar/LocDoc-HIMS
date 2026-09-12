package com.lockdoc.doctor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * One row per ACCEPTED {@code DoctorFacilityMapping}, whether or not a
 * status has actually been set today — a facility a doctor is mapped to
 * but hasn't touched yet still needs to show up as "Unavailable" (the
 * enum's own resting state, §8.3), not be silently missing from the list.
 * {@code lastUpdated} is null exactly when no status row exists yet today.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorStatusTodayResponse {

    private Long mappingId;
    private Long facilityId;
    private String facilityName;
    private String status;
    private String source;
    private LocalDateTime lastUpdated;
}
