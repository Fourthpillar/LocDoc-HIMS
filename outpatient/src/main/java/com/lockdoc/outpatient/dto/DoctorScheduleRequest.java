package com.lockdoc.outpatient.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DoctorScheduleRequest {

    @NotNull(message = "doctorId is required")
    private Long doctorId;

    /**
     * Required only when the caller is a doctor self-scheduling (their JWT
     * carries no facilityId, §4, and they may be mapped to several) - a
     * Hospital/Clinic Admin's facility instead comes from their own
     * token via {@code SecurityUtils.requireFacilityId()}, so this is
     * ignored on that path even if sent.
     */
    private Long facilityId;

    @NotBlank(message = "weekday is required")
    private String weekday;

    @NotBlank(message = "sessionName is required")
    private String sessionName;

    @NotNull(message = "startTime is required")
    private LocalTime startTime;

    @NotNull(message = "endTime is required")
    private LocalTime endTime;

    @NotNull(message = "capacity is required")
    @Positive(message = "capacity must be greater than zero")
    private Integer capacity;

    private Integer overbookAllowance;

    @NotNull(message = "effectiveFrom is required")
    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;
}
