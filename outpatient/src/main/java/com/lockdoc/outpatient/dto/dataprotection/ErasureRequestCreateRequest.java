package com.lockdoc.outpatient.dto.dataprotection;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Master Spec §18.4 - staff-actioned intake; arrives by phone/in-person/written, never a patient self-service submission. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ErasureRequestCreateRequest {

    @NotNull(message = "patientId is required")
    private Long patientId;

    @NotBlank(message = "requestedVia is required")
    private String requestedVia;

    @NotBlank(message = "requestedBy is required")
    private String requestedBy;
}
