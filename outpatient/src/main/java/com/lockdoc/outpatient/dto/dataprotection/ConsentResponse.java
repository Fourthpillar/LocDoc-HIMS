package com.lockdoc.outpatient.dto.dataprotection;

import com.lockdoc.outpatient.entity.dataprotection.Consent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsentResponse {

    private Long id;
    private Long patientId;
    private String patientName;
    private String patientMrn;
    private String consentType;
    private Integer version;
    private String textShown;
    private Long capturedByUserId;
    private LocalDateTime capturedAt;
    private String method;
    private String guardianName;
    private String guardianRelation;

    public static ConsentResponse toResponse(Consent c) {
        boolean patientStillExists = c.getPatient() != null;
        return ConsentResponse.builder()
                .id(c.getId())
                .patientId(patientStillExists ? c.getPatient().getId() : null)
                .patientName(patientStillExists ? c.getPatient().getFullName() : c.getPatientNameSnapshot() + " (erased)")
                .patientMrn(patientStillExists ? c.getPatient().getMrn() : c.getPatientMrnSnapshot())
                .consentType(c.getConsentType())
                .version(c.getVersion())
                .textShown(c.getTextShown())
                .capturedByUserId(c.getCapturedByUserId())
                .capturedAt(c.getCapturedAt())
                .method(c.getMethod())
                .guardianName(c.getGuardianName())
                .guardianRelation(c.getGuardianRelation())
                .build();
    }
}
