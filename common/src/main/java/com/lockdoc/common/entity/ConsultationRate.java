package com.lockdoc.common.entity;

import com.lockdoc.common.entity.Doctor;
import com.lockdoc.common.entity.Facility;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Doctor-proposed, Hospital/Clinic Admin-approved consultation fee
 * (Master Spec §8.6/§6). Unlike procedure/service rates (Hospital/Clinic
 * Admin sets those outright), this one is proposed here and only becomes
 * billable once approved — build order step 4 builds the doctor-side
 * propose half only; the approve half (and the OP billing lookup of the
 * current approved rate) ships with step 5, which is why there is no
 * approve/reject method or endpoint yet on this entity's owning service.
 *
 * {@code medicalDepartmentId}/{@code specializationId} from §6's full
 * dimension list are intentionally omitted here — those master tables
 * don't exist until the OP Module (step 5/§7.2), and a doctor's fee
 * proposal shouldn't be blocked waiting on them.
 */
@Entity
@Table(name = "consultation_rates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationRate {

    public static final String ORG_TYPE_DIRECT = "DIRECT";
    public static final String ORG_TYPE_ORGANIZATION = "ORGANIZATION";
    public static final String ORG_TYPE_TPA = "TPA";

    public static final String DAY = "DAY";
    public static final String NIGHT = "NIGHT";

    public static final String STATUS_PENDING_APPROVAL = "PENDING_APPROVAL";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id", nullable = false)
    private Facility facility;

    @Column(name = "org_type", nullable = false, length = 20)
    @Builder.Default
    private String orgType = ORG_TYPE_DIRECT;

    @Column(name = "day_night_indicator", nullable = false, length = 10)
    @Builder.Default
    private String dayNightIndicator = DAY;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "hospital_percent", precision = 5, scale = 2)
    private BigDecimal hospitalPercent;

    @Column(name = "effective_from")
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = STATUS_PENDING_APPROVAL;

    @Column(name = "proposed_by_user_id", nullable = false)
    private Long proposedByUserId;

    @Column(name = "approved_by_user_id")
    private Long approvedByUserId;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejection_reason", length = 300)
    private String rejectionReason;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdDate = now;
        this.updatedDate = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedDate = LocalDateTime.now();
    }
}
