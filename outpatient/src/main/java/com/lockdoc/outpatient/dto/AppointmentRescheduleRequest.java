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
public class AppointmentRescheduleRequest {

    @NotNull(message = "newAppointmentTs is required")
    private LocalDateTime newAppointmentTs;

    /** Omit to keep the same doctor schedule and just move the time. */
    private Long newDoctorScheduleId;
}
