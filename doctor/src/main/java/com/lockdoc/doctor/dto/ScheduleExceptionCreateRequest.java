package com.lockdoc.doctor.dto;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/** Commits the block from {@link ScheduleExceptionRequest} together with a decision for every appointment it affects (§8.2). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleExceptionCreateRequest extends ScheduleExceptionRequest {

    @Valid
    private List<AppointmentResolutionRequest> resolutions;
}
