package com.lockdoc.app.dto.doctor;

import com.lockdoc.app.entity.doctor.Prescription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrescriptionResponse {

    private Long id;
    private Long opVisitId;
    private Boolean isDraft;
    private List<PrescriptionLineResponse> lines;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    public static PrescriptionResponse toResponse(Prescription p, List<PrescriptionLineResponse> lines) {
        return PrescriptionResponse.builder()
                .id(p.getId())
                .opVisitId(p.getOpVisit().getId())
                .isDraft(p.getIsDraft())
                .lines(lines)
                .createdDate(p.getCreatedDate())
                .updatedDate(p.getUpdatedDate())
                .build();
    }
}
