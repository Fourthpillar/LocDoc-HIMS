package com.lockdoc.outpatient.entity;

import com.lockdoc.common.entity.Facility;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Master Spec §6/§7.5/§4.1 - unconditionally Pending Approval when
 * requested by a Receptionist ("request only", no threshold, unlike
 * Discount above) - a Hospital/Clinic Admin's own request auto-resolves
 * approved in the service layer, matching how they already hold both
 * the request and approve rights.
 */
@Entity
@Table(name = "cancellations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cancellation {

    public static final String ENTITY_APPOINTMENT = "APPOINTMENT";
    public static final String ENTITY_BILL = "BILL";
    /** An OP visit itself (§8.7/§7.5) - the doctor's own "Cancel Consultation" action, always Pending Approval since a doctor never holds the approve right. */
    public static final String ENTITY_OP_VISIT = "OP_VISIT";

    public static final String STATUS_PENDING_APPROVAL = "PENDING_APPROVAL";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id", nullable = false)
    private Facility facility;

    @Column(name = "entity_type", nullable = false, length = 20)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(name = "reason", nullable = false, length = 300)
    private String reason;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = STATUS_PENDING_APPROVAL;

    @Column(name = "requested_by_user_id", nullable = false)
    private Long requestedByUserId;

    @Column(name = "approved_by_user_id")
    private Long approvedByUserId;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
    }
}
