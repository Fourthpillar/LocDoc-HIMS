package com.lockdoc.app.dto.doctor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * The printed OP card (Master Spec §7.4/§9, §8.7) — one document, not two:
 * the top half auto-populates from registration and billing the moment a
 * patient registers, and the bottom half (the consultation narrative +
 * Prescription Medicines table) fills in once the doctor completes the
 * visit. Assembled server-side from Patient/PatientRegistration/Bill/
 * Doctor/ConsultationNote/Prescription in one call rather than stitched
 * from several client-side round trips, since §9's field list is sourced
 * from five different entities and getting that wrong prints a wrong card.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpCardResponse {

    private Long opVisitId;
    private String opNo;
    private String visitType;
    private Boolean mlcFlag;
    private LocalDateTime arrivedTs;

    private String patientName;
    private String patientMrn;
    private LocalDate patientDob;
    private Integer patientAge;
    private String patientGender;
    private String attendantName;
    private String attendantMobile;
    private String attendantRelation;

    private String facilityName;
    private String doctorName;
    private String doctorRegistrationNumber;
    private String doctorSpecialties;

    /** Null if this patient has no registration on file at this facility yet (walk-in edge case). */
    private String registrationNo;
    private LocalDate registrationExpiryDate;

    /** The consultation's own bill (encounterType=CONSULTATION) — null until it's been billed. */
    private String billNo;
    private LocalDateTime billDate;
    private BigDecimal billAmount;
    private String payorType;

    private BigDecimal weightKg;
    private BigDecimal heightCm;
    private BigDecimal temperatureF;
    private String bp;

    /** Null until the doctor has drafted a note for this visit at all. */
    private ConsultationNoteResponse note;
    /** Null until at least a prescription draft exists; empty list is a real "nothing prescribed", not "not authored yet". */
    private PrescriptionResponse prescription;
    /**
     * True while either the note or the prescription is still a draft — the print carries a
     * "DRAFT" watermark until both are completed. Boolean wrapper, not primitive - a primitive
     * boolean isDraft field gets Lombok's isDraft() getter, which Jackson's default naming
     * strips the "is" from, serializing this as "draft" instead of "isDraft" and silently
     * breaking the frontend's `card.isDraft` read. Boolean gets getIsDraft() instead, which
     * doesn't get stripped - same reason every other isDraft field in this codebase
     * (ConsultationNote, Prescription) already uses the wrapper type, not the primitive.
     */
    private Boolean isDraft;
}
