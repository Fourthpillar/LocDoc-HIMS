package com.lockdoc.app.dto.dataprotection;

import com.lockdoc.app.entity.dataprotection.ErasureRequest;
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
public class ErasureRequestResponse {

    private Long id;
    private Long patientId;
    private String patientName;
    private String patientMrn;
    private LocalDateTime requestedAt;
    private String requestedVia;
    private String requestedBy;
    private String status;
    private Long reviewedByUserId;
    private LocalDateTime resolvedAt;
    private String resolutionNotes;
    private LocalDateTime createdDate;

    public static ErasureRequestResponse toResponse(ErasureRequest r) {
        boolean patientStillExists = r.getPatient() != null;
        return ErasureRequestResponse.builder()
                .id(r.getId())
                .patientId(patientStillExists ? r.getPatient().getId() : null)
                .patientName(patientStillExists ? r.getPatient().getFullName() : r.getPatientNameSnapshot() + " (erased)")
                .patientMrn(patientStillExists ? r.getPatient().getMrn() : r.getPatientMrnSnapshot())
                .requestedAt(r.getRequestedAt())
                .requestedVia(r.getRequestedVia())
                .requestedBy(r.getRequestedBy())
                .status(r.getStatus())
                .reviewedByUserId(r.getReviewedByUserId())
                .resolvedAt(r.getResolvedAt())
                .resolutionNotes(r.getResolutionNotes())
                .createdDate(r.getCreatedDate())
                .build();
    }
}
