package com.lockdoc.app.dto.doctor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Hospital/Clinic Admin inviting a verified doctor to their facility (Master Spec §4.1/§8.1). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FacilityMappingInviteRequest {

    @NotNull(message = "doctorId is required")
    private Long doctorId;

    @NotBlank(message = "relationshipType is required")
    private String relationshipType;
}
