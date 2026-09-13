package com.lockdoc.outpatient.entity;

import com.lockdoc.common.entity.Facility;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Self-service (Master Spec §6, §16 step 8) - Hospital/Clinic Admin sets
 * this per referrer from their own login, same self-service pattern as
 * {@link ApprovalPolicy}. {@code partyName} is a free-text label rather
 * than an FK to ReferralDoctor/PRO - those master tables (§6 Masters,
 * screen #34) aren't built yet, see V35's migration comment.
 */
@Entity
@Table(name = "commission_basis")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommissionBasis {

    public static final String PARTY_REFERRAL_DOCTOR = "REFERRAL_DOCTOR";
    public static final String PARTY_PRO = "PRO";

    public static final String BASIS_FIXED = "FIXED";
    public static final String BASIS_PERCENT = "PERCENT";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id", nullable = false)
    private Facility facility;

    @Column(name = "party_type", nullable = false, length = 40)
    private String partyType;

    @Column(name = "party_name", nullable = false, length = 150)
    private String partyName;

    @Column(name = "basis", nullable = false, length = 10)
    private String basis;

    @Column(name = "value_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal value;

    @Column(name = "applies_to", length = 200)
    private String appliesTo;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

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
