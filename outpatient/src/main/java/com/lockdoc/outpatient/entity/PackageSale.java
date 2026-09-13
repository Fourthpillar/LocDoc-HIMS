package com.lockdoc.outpatient.entity;

import com.lockdoc.common.entity.Facility;
import com.lockdoc.outpatient.entity.Patient;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * A package sold to a patient (Master Spec §7.5) - the fourth billable
 * OP encounter, {@link Bill#ENCOUNTER_PACKAGE}'s target row. Mirrors
 * {@link ProcedureBill}'s shape and its same two-phase-save need (see
 * V41's migration comment) - {@link #billId} starts null, set right
 * after the {@link Bill} is created in the same transaction.
 */
@Entity
@Table(name = "package_sales")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackageSale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id", nullable = false)
    private Facility facility;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id", nullable = false)
    private Package packageEntity;

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
