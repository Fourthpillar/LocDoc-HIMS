package com.lockdoc.app.dto.doctor;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * What the doctor is proposing to block - or, for {@code SESSION_MOVE}, move
 * to a new time (Master Spec §8.2). Used both to preview affected
 * appointments and, extended by {@link ScheduleExceptionCreateRequest}, to
 * actually commit the change.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleExceptionRequest {

    @NotNull(message = "facilityId is required")
    private Long facilityId;

    /** Null blocks every session this doctor has at facilityId on exceptionDate; set targets just that one session. */
    private Long doctorScheduleId;

    @NotNull(message = "exceptionDate is required")
    private LocalDate exceptionDate;

    @NotNull(message = "type is required")
    private String type;

    private String reason;

    /** Required (with newEndTime) only when type is SESSION_MOVE - the proposed new time for this occurrence. */
    private LocalTime newStartTime;

    private LocalTime newEndTime;
}
