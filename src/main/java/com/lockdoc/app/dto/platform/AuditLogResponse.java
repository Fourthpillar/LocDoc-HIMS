package com.lockdoc.app.dto.platform;

import com.lockdoc.app.entity.platform.AuditLogEntry;
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
public class AuditLogResponse {

    private Long id;
    private Long facilityId;
    private Long actorUserId;
    private String action;
    private String entityType;
    private Long entityId;
    private String beforeData;
    private String afterData;
    private LocalDateTime occurredAt;

    public static AuditLogResponse toResponse(AuditLogEntry e) {
        return AuditLogResponse.builder()
                .id(e.getId())
                .facilityId(e.getFacilityId())
                .actorUserId(e.getActorUserId())
                .action(e.getAction())
                .entityType(e.getEntityType())
                .entityId(e.getEntityId())
                .beforeData(e.getBeforeData())
                .afterData(e.getAfterData())
                .occurredAt(e.getOccurredAt())
                .build();
    }
}
