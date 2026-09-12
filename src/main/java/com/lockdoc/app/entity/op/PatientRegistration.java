package com.lockdoc.app.entity.op;

import com.lockdoc.app.entity.Facility;
import com.lockdoc.app.entity.pharmacy.Patient;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * A billable registration/re-registration event (Master Spec §6/§7.4).
 * Deliberately does NOT carry fee/discount/net/paid/due directly, unlike
 * §6's literal field list on this entity - that money lives once, on
 * {@link Bill} (encounterType=REGISTRATION, encounterId=this row's id),
 * so a registration's numbers can never drift out of sync between two
 * tables. Observable behaviour is identical; only the storage shape
 * differs from the spec's literal wording.
 */
@Entity
@Table(name = "patient_registrations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id", nullable = false)
    private Facility facility;

    @Column(name = "registration_no", nullable = false, length = 30)
    private String registrationNo;

    @Column(name = "is_re_registration", nullable = false)
    @Builder.Default
    private Boolean isReRegistration = false;

    /** Null when the facility's registration validity is "never expires" (§7.2) — this one stays valid for good. */
    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "registered_at", nullable = false)
    private LocalDateTime registeredAt;

    @Column(name = "registered_by_user_id", nullable = false)
    private Long registeredByUserId;

    @PrePersist
    protected void onCreate() {
        if (this.registeredAt == null) {
            this.registeredAt = LocalDateTime.now();
        }
    }
}
