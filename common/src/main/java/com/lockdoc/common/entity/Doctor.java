package com.lockdoc.common.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * A practising clinician's profile - authentication itself lives on the
 * linked {@link User} row (one-to-one), matching every other role's
 * login mechanism (Master Spec §17.4). Deliberately not facility-scoped:
 * a doctor's registration/council credentials are verified once, centrally,
 * here - which facilities they practise at is DoctorFacilityMapping,
 * built in a later step (Master Spec §8.1), not a column on this entity.
 */
@Entity
@Table(name = "doctors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Doctor {

    public static final String VERIFICATION_PENDING = "PENDING";
    public static final String VERIFICATION_VERIFIED = "VERIFIED";
    public static final String VERIFICATION_REJECTED = "REJECTED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    // NMC / state medical council registration number - centrally
    // verified once (Master Spec §8.1), never re-verified per facility.
    @Column(name = "registration_number", nullable = false, length = 100)
    private String registrationNumber;

    @Column(name = "verification_status", nullable = false, length = 20)
    @Builder.Default
    private String verificationStatus = VERIFICATION_PENDING;

    @Column(name = "specialties", length = 300)
    private String specialties;

    /** Doctor profile (Master Spec §8.9, screen #19) - self-editable, unlike registrationNumber/verificationStatus which stay Super-Admin-owned. */
    @Column(name = "qualifications", length = 300)
    private String qualifications;

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
