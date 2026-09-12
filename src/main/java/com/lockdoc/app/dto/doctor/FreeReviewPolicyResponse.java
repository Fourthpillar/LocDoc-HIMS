package com.lockdoc.app.dto.doctor;

import com.lockdoc.app.entity.doctor.FreeReviewPolicy;
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
public class FreeReviewPolicyResponse {

    private Long id;
    private Long doctorId;
    private String doctorName;
    private Integer maxDays;
    private Integer maxVisits;
    private LocalDateTime updatedDate;

    public static FreeReviewPolicyResponse toResponse(FreeReviewPolicy p) {
        return FreeReviewPolicyResponse.builder()
                .id(p.getId())
                .doctorId(p.getDoctor().getId())
                .doctorName(p.getDoctor().getFullName())
                .maxDays(p.getMaxDays())
                .maxVisits(p.getMaxVisits())
                .updatedDate(p.getUpdatedDate())
                .build();
    }
}
