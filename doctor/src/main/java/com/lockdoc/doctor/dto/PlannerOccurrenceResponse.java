package com.lockdoc.doctor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * One occurrence of a recurring {@code DoctorSchedule} session, expanded
 * onto a single calendar date for the Availability Planner (§8.2, screen
 * #14) - the unit the day/week/month grid is built from.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlannerOccurrenceResponse {

    private LocalDate date;
    private Long doctorScheduleId;
    private Long facilityId;
    private String facilityName;
    private String sessionName;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer capacity;
    private Integer overbookAllowance;
    private Integer bookedCount;
    /** The recurring session's own date range - what "repeats every Monday from … until …" is read from. */
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private boolean blocked;
    private Long exceptionId;
    private String exceptionType;
    private String exceptionReason;
    /** The block on this date covers every session at the facility (a whole-day block), not just this one - undoing it restores them all. */
    private boolean exceptionCoversWholeDay;
    /** Set only when exceptionType is SESSION_MOVE - the proposed new time for this occurrence. */
    private LocalTime movedToStartTime;
    private LocalTime movedToEndTime;
}
