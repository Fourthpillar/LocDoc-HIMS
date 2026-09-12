package com.lockdoc.doctor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Reception's status correction (Master Spec §8.3) - attached to the
 * front-desk day list, build order step 5. Covers the real case of a
 * doctor mid-consultation and not touching their phone; attributed
 * (setByUserId) and audit-logged like any other status change, just with
 * source=RECEPTION instead of SELF.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DoctorStatusOverrideRequest {

    @NotNull(message = "doctorId is required")
    private Long doctorId;

    @NotBlank(message = "status is required")
    private String status;

    /**
     * Which facility the status is recorded against. Ignored for a user who belongs to
     * one; for Super Admin it is optional, and only needed when the doctor practises at
     * more than one facility.
     */
    private Long facilityId;
}
