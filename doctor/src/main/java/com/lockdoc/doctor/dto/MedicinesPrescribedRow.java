package com.lockdoc.doctor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicinesPrescribedRow {
    private String medicineName;
    private int timesPrescribed;
    private int distinctPatients;

    // "Usual" = the value the doctor entered most often for this medicine in the
    // range. Null when that field was never filled in. These are free text on
    // the prescription line, so they're reported as written, not normalised.
    private String genericName;
    private String usualStrength;
    private String usualDosage;
    private String usualFrequency;
    private String usualDuration;
    private String usualRoute;

    /** Sum of the quantity written on every line — lines with no quantity contribute nothing. */
    private int totalQuantity;
    private LocalDateTime lastPrescribedAt;
}
