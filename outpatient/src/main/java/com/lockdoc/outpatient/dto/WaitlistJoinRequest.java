package com.lockdoc.outpatient.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WaitlistJoinRequest {

    @NotNull(message = "patientId is required")
    private Long patientId;

    @NotNull(message = "doctorScheduleId is required")
    private Long doctorScheduleId;

    @NotNull(message = "sessionDate is required")
    private LocalDate sessionDate;
}
