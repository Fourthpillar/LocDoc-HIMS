package com.lockdoc.doctor.dto;

import com.lockdoc.doctor.entity.FreeReviewPolicy;
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
    /** Which facility this policy is for — a doctor can have a different one at each. */
    private Long facilityId;
    private String facilityName;
    private Integer maxDays;
    private Integer maxVisits;
    private LocalDateTime updatedDate;

    public static FreeReviewPolicyResponse toResponse(FreeReviewPolicy p) {
        return FreeReviewPolicyResponse.builder()
                .id(p.getId())
                .doctorId(p.getDoctor().getId())
                .doctorName(p.getDoctor().getFullName())
                .facilityId(p.getFacility().getId())
                .facilityName(p.getFacility().getName())
                .maxDays(p.getMaxDays())
                .maxVisits(p.getMaxVisits())
                .updatedDate(p.getUpdatedDate())
                .build();
    }
}
