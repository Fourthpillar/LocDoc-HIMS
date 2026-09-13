package com.lockdoc.outpatient.dto;

import com.lockdoc.outpatient.entity.Cancellation;
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
public class CancellationResponse {

    private Long id;
    private String entityType;
    private Long entityId;
    private String reason;
    private String status;
    private Long requestedByUserId;
    private Long approvedByUserId;
    private LocalDateTime approvedAt;
    private LocalDateTime createdDate;
    /** Who/what this is actually about, resolved by CancellationService for legibility — "Anita Rao (PT-2026-000002)" beats "OP_VISIT #193". Null where not resolved. */
    private String subjectLabel;

    public static CancellationResponse toResponse(Cancellation c) {
        return toResponse(c, null);
    }

    public static CancellationResponse toResponse(Cancellation c, String subjectLabel) {
        return CancellationResponse.builder()
                .id(c.getId())
                .entityType(c.getEntityType())
                .entityId(c.getEntityId())
                .reason(c.getReason())
                .status(c.getStatus())
                .requestedByUserId(c.getRequestedByUserId())
                .approvedByUserId(c.getApprovedByUserId())
                .approvedAt(c.getApprovedAt())
                .createdDate(c.getCreatedDate())
                .subjectLabel(subjectLabel)
                .build();
    }
}
