package com.lockdoc.app.dto.op;

import com.lockdoc.app.entity.op.ConsultationRating;
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
public class ConsultationRatingResponse {

    private Long id;
    private Long opVisitId;
    private Integer rating;
    private String comment;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    public static ConsultationRatingResponse toResponse(ConsultationRating r) {
        return ConsultationRatingResponse.builder()
                .id(r.getId())
                .opVisitId(r.getOpVisit().getId())
                .rating(r.getRating())
                .comment(r.getComment())
                .createdDate(r.getCreatedDate())
                .updatedDate(r.getUpdatedDate())
                .build();
    }
}
