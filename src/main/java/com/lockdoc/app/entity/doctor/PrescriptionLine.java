package com.lockdoc.app.entity.doctor;

import jakarta.persistence.*;
import lombok.*;

/**
 * Master Spec §6/§8.7. {@code matchedPharmacyMedicineId} is nullable and
 * populated only when the facility's Pharmacy module is active and a
 * catalogue match is found (§8.7's module-independence requirement) — a
 * soft reference, not a JPA relation, matching the existing
 * MedicineBatch.sourcePurchaseItemId pattern for the same reason
 * (Pharmacy is a different module's table, referenced loosely).
 */
@Entity
@Table(name = "prescription_lines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrescriptionLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_id", nullable = false)
    private Prescription prescription;

    @Column(name = "line_order", nullable = false)
    @Builder.Default
    private Integer lineOrder = 0;

    @Column(name = "medicine_name", nullable = false, length = 200)
    private String medicineName;

    @Column(name = "generic_name", length = 200)
    private String genericName;

    @Column(name = "strength", length = 50)
    private String strength;

    @Column(name = "dosage", length = 100)
    private String dosage;

    @Column(name = "route", length = 50)
    private String route;

    @Column(name = "frequency", length = 50)
    private String frequency;

    @Column(name = "duration", length = 50)
    private String duration;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "refill_flag", nullable = false)
    @Builder.Default
    private Boolean refillFlag = false;

    @Column(name = "matched_pharmacy_medicine_id")
    private Long matchedPharmacyMedicineId;
}
