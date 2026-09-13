package com.lockdoc.outpatient.dto;

import com.lockdoc.outpatient.entity.Organization;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationResponse {

    private Long id;
    private String name;
    private String orgType;
    private String contractTerms;
    private String creditTerms;
    private String authorisationRefFormat;
    private Boolean active;

    public static OrganizationResponse toResponse(Organization o) {
        return OrganizationResponse.builder()
                .id(o.getId()).name(o.getName()).orgType(o.getOrgType())
                .contractTerms(o.getContractTerms()).creditTerms(o.getCreditTerms())
                .authorisationRefFormat(o.getAuthorisationRefFormat()).active(o.getActive())
                .build();
    }
}
