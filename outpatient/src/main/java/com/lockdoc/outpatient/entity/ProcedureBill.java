package com.lockdoc.outpatient.entity;

import com.lockdoc.common.entity.Doctor;
import com.lockdoc.common.entity.Facility;
import com.lockdoc.outpatient.entity.Patient;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * A billed procedure/service (Master Spec §7.5) - the third billable OP
 * encounter alongside registration and consultation, {@link Bill}'s
 * {@code PROCEDURE} encounter type's target row. {@link #patient} is
 * null for an unregistered walk-in (V44) - the walk-in's own
 * name/age/gender/mobile are captured on {@link #walkInName} etc.
 * instead, per §7.5. Exactly one of {@code patient} or
 * {@code walkInName} is set, never both, never neither - enforced in
 * {@code ProcedureBillingService}, not at the schema level. {@link #doctor}
 * is optional per §7.5 ("not tied to a doctor_id at all");
 * {@link #performedByName} is free text rather than an Employee_id FK
 * since Employee/Designation don't exist yet.
 */
@Entity
@Table(name = "procedure_bills")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcedureBill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id", nullable = false)
    private Facility facility;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "billable_item_id", nullable = false)
    private BillableItem billableItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    @Column(name = "org_type", nullable = false, length = 20)
    private String orgType;

    @Column(name = "performed_by_name", length = 150)
    private String performedByName;

    @Column(name = "walk_in_name", length = 150)
    private String walkInName;

    @Column(name = "walk_in_age")
    private Integer walkInAge;

    @Column(name = "walk_in_gender", length = 10)
    private String walkInGender;

    @Column(name = "walk_in_mobile", length = 20)
    private String walkInMobile;

    /**
     * Set in a second save right after the first (see V40's migration
     * comment) - nullable at the schema level only for that brief
     * intra-transaction moment; every row that survives its own
     * transaction has one.
     */
    @Column(name = "bill_id")
    private Long billId;

    @Column(name = "created_by_user_id", nullable = false)
    private Long createdByUserId;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
    }
}
