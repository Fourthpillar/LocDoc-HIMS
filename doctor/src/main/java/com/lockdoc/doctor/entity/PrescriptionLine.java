package com.lockdoc.doctor.entity;

import jakarta.persistence.*;
import lombok.*;

/** Master Spec §6/§8.7 - one free-text medicine line on a prescription. */
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
}
