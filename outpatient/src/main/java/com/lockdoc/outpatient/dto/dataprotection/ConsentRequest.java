package com.lockdoc.outpatient.dto.dataprotection;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Master Spec §18.2 - "Consent given by [patient/guardian name], witnessed by [staff name]" is captured the moment this is submitted, not a piece of paper filed later. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConsentRequest {

    @NotNull(message = "patientId is required")
    private Long patientId;

    @NotBlank(message = "consentType is required")
    private String consentType;

    @NotBlank(message = "textShown is required")
    private String textShown;

    private String method;
    private String guardianName;
    private String guardianRelation;
}
