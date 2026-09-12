package com.lockdoc.app.dto.doctor;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * The doctor's explicit reschedule-or-cancel decision for one appointment
 * a proposed schedule block would otherwise silently orphan (§8.2).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResolutionRequest {

    public static final String ACTION_RESCHEDULE = "RESCHEDULE";
    public static final String ACTION_CANCEL = "CANCEL";

    @NotNull(message = "appointmentId is required")
    private Long appointmentId;

    @NotNull(message = "action is required")
    private String action;

    /** RESCHEDULE only. */
    private LocalDateTime newAppointmentTs;

    /** RESCHEDULE only - omit to keep the same doctor schedule and just move the time. */
    private Long newDoctorScheduleId;

    /** CANCEL only. */
    private String reason;
}
