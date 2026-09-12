package com.lockdoc.app.entity.dataprotection;

import com.lockdoc.app.entity.Facility;
import com.lockdoc.app.entity.pharmacy.Patient;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DPDP erasure/grievance intake (Master Spec §18.4/§18.6) - arrives
 * offline (phone/in-person/written), logged and actioned by
 * Hospital/Clinic Admin, never automatic. Target: resolved within 30
 * days of {@code requestedAt} (pilot default, §18.7) - the UI's
 * SlaCountdownBadge (§17.6) is computed off that field, not stored here.
 */
@Entity
@Table(name = "erasure_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErasureRequest {

    public static final String VIA_IN_PERSON = "IN_PERSON";
    public static final String VIA_PHONE = "PHONE";
    public static final String VIA_WRITTEN = "WRITTEN";

    public static final String BY_SELF = "SELF";
    public static final String BY_GUARDIAN = "GUARDIAN";

    public static final String STATUS_RECEIVED = "RECEIVED";
    public static final String STATUS_UNDER_REVIEW = "UNDER_REVIEW";
    public static final String STATUS_FULFILLED_ANONYMIZED = "FULFILLED_ANONYMIZED";
    public static final String STATUS_FULFILLED_DELETED = "FULFILLED_DELETED";
    public static final String STATUS_REJECTED_RETENTION_REQUIRED = "REJECTED_RETENTION_REQUIRED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id", nullable = false)
    private Facility facility;

    /** Nullable, ON DELETE SET NULL - "Fulfilled (deleted)" hard-deletes this patient; {@link #patientNameSnapshot}/{@link #patientMrnSnapshot} keep the row legible afterward. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @Column(name = "patient_name_snapshot", nullable = false, length = 150)
    private String patientNameSnapshot;

    @Column(name = "patient_mrn_snapshot", nullable = false, length = 30)
    private String patientMrnSnapshot;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "requested_via", nullable = false, length = 20)
    private String requestedVia;

    @Column(name = "requested_by", nullable = false, length = 20)
    private String requestedBy;

    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private String status = STATUS_RECEIVED;

    @Column(name = "reviewed_by_user_id")
    private Long reviewedByUserId;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "resolution_notes", length = 1000)
    private String resolutionNotes;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
        if (this.requestedAt == null) {
            this.requestedAt = this.createdDate;
        }
    }
}
