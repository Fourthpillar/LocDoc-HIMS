package com.lockdoc.app.dto.doctor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

/** One weekday window the doctor says they will consult in (Master Spec §8.2). */
@Getter
@Setter
public class ConsultationHourRequest {

    @NotBlank(message = "weekday is required")
    private String weekday;

    @NotNull(message = "startTime is required")
    private LocalTime startTime;

    @NotNull(message = "endTime is required")
    private LocalTime endTime;
}
