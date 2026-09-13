package com.lockdoc.outpatient.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentRequest {

    @NotNull(message = "patientId is required")
    private Long patientId;

    @NotNull(message = "doctorScheduleId is required")
    private Long doctorScheduleId;

    @NotNull(message = "appointmentTs is required")
    private LocalDateTime appointmentTs;

    /** CONSULTATION (default) or PROCEDURE — decides whether arrival raises the doctor's fee (§7.5). */
    private String purpose;

    private String channel;
}
