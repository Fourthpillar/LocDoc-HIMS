package com.lockdoc.common.dto.platform;

import com.lockdoc.common.entity.platform.SupportTicket;
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
public class SupportTicketResponse {

    private Long id;
    private Long facilityId;
    private String facilityName;
    private Long raisedByUserId;
    private String category;
    private String subject;
    private String description;
    private String status;
    private String resolutionNotes;
    private Long resolvedByUserId;
    private LocalDateTime resolvedAt;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    public static SupportTicketResponse toResponse(SupportTicket t) {
        return SupportTicketResponse.builder()
                .id(t.getId())
                .facilityId(t.getFacility().getId())
                .facilityName(t.getFacility().getName())
                .raisedByUserId(t.getRaisedByUserId())
                .category(t.getCategory())
                .subject(t.getSubject())
                .description(t.getDescription())
                .status(t.getStatus())
                .resolutionNotes(t.getResolutionNotes())
                .resolvedByUserId(t.getResolvedByUserId())
                .resolvedAt(t.getResolvedAt())
                .createdDate(t.getCreatedDate())
                .updatedDate(t.getUpdatedDate())
                .build();
    }
}
