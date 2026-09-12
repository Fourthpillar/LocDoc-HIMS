package com.lockdoc.app.entity.platform;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Cross-facility audit log (Master Spec §10, §6, screen #42) - Super
 * Admin's read-only trail. Deliberately NOT wired into every write in
 * the app (that would be a cross-cutting change well beyond one step's
 * scope) - see {@code AuditLogService}'s own javadoc for exactly which
 * actions actually write here.
 */
@Entity
@Table(name = "audit_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Null for a genuinely platform-level action (e.g. facility onboarding, before any facility_id exists to attribute it to). */
    @Column(name = "facility_id")
    private Long facilityId;

    @Column(name = "actor_user_id")
    private Long actorUserId;

    @Column(name = "action", nullable = false, length = 60)
    private String action;

    @Column(name = "entity_type", nullable = false, length = 60)
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "before_data", length = 2000)
    private String beforeData;

    @Column(name = "after_data", length = 2000)
    private String afterData;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    @PrePersist
    protected void onCreate() {
        if (this.occurredAt == null) {
            this.occurredAt = LocalDateTime.now();
        }
    }
}
