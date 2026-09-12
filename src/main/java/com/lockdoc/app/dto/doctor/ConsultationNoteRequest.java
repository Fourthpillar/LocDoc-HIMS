package com.lockdoc.app.dto.doctor;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Draft or final save of the clinical note (Master Spec §8.7) — every
 * field optional here since the frontend auto-saves a partially-filled
 * form every ~15s/on-blur; validation that the note is actually usable
 * belongs at "complete", not at every draft tick.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationNoteRequest {

    private String chiefComplaint;
    private String pastHistory;
    private String familyHistory;
    private String nutritionalHistory;
    private String developmentalHistory;
    private String examinationFindings;
    private String provisionalDiagnosis;
    private String investigationsOrdered;
    private String treatmentPlan;
    private String patientFamilyEducation;
    private String followUpPlan;
    private String admitTo;
}
