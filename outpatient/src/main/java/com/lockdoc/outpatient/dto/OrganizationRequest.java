package com.lockdoc.outpatient.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationRequest {

    @NotBlank(message = "name is required")
    private String name;

    @NotBlank(message = "orgType is required")
    private String orgType;

    private String contractTerms;
    private String creditTerms;
    private String authorisationRefFormat;
}
