package com.lockdoc.app.dto.doctor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Doctor sets their own status (Master Spec §8.3) — sessionDate is not
 * client-supplied, it is always "today" in IST, resolved server-side
 * (Master Spec §17.2's fixed-IST decision), so a client clock can never
 * misattribute a status change to the wrong day.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DoctorStatusRequest {

    @NotNull(message = "facilityId is required")
    private Long facilityId;

    @NotBlank(message = "status is required")
    private String status;
}
