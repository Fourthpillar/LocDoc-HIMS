package com.lockdoc.outpatient.entity.dataprotection;

import com.lockdoc.common.entity.Facility;
import com.lockdoc.outpatient.entity.Patient;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Consent captured, not assumed (Master Spec §18.2) - treatment and
 * data-processing consent are two separate rows per capture, each
 * versioned; {@code textShown} is the exact wording shown at capture
 * time, stored so a later wording revision never retroactively changes
 * what an earlier patient is recorded as having agreed to.
 */
@Entity
@Table(name = "consents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Consent {

    public static final String TYPE_TREATMENT = "TREATMENT";
    public static final String TYPE_DATA_PROCESSING = "DATA_PROCESSING";

    public static final String METHOD_IN_APP_CHECKBOX = "IN_APP_CHECKBOX";
    public static final String METHOD_SIGNATURE_PAD = "SIGNATURE_PAD";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id", nullable = false)
    private Facility facility;

    /** Nullable, ON DELETE SET NULL - a "Fulfilled (deleted)" erasure resolution can hard-delete this patient; {@link #patientNameSnapshot}/{@link #patientMrnSnapshot} keep the record legible afterward. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @Column(name = "patient_name_snapshot", nullable = false, length = 150)
    private String patientNameSnapshot;

    @Column(name = "patient_mrn_snapshot", nullable = false, length = 30)
    private String patientMrnSnapshot;

    @Column(name = "consent_type", nullable = false, length = 20)
    private String consentType;

    @Column(name = "version", nullable = false)
    @Builder.Default
    private Integer version = 1;

    @Lob
    @Column(name = "text_shown", nullable = false)
    private String textShown;

    @Column(name = "captured_by_user_id", nullable = false)
    private Long capturedByUserId;

    @Column(name = "captured_at", nullable = false)
    private LocalDateTime capturedAt;

    @Column(name = "method", nullable = false, length = 20)
    @Builder.Default
    private String method = METHOD_IN_APP_CHECKBOX;

    /** Populated only when the patient is a minor - the attendant captured at registration (§7.4) is the consenting guardian (§18.5). */
    @Column(name = "guardian_name", length = 150)
    private String guardianName;

    @Column(name = "guardian_relation", length = 50)
    private String guardianRelation;

    @PrePersist
    protected void onCreate() {
        if (this.capturedAt == null) {
            this.capturedAt = LocalDateTime.now();
        }
    }
}
