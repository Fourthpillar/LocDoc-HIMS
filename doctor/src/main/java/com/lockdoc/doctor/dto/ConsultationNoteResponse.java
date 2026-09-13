package com.lockdoc.doctor.dto;

import com.lockdoc.doctor.entity.ConsultationNote;
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
public class ConsultationNoteResponse {

    private Long id;
    private Long opVisitId;
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
    private Boolean isDraft;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    public static ConsultationNoteResponse toResponse(ConsultationNote n) {
        return ConsultationNoteResponse.builder()
                .id(n.getId())
                .opVisitId(n.getOpVisit().getId())
                .chiefComplaint(n.getChiefComplaint())
                .pastHistory(n.getPastHistory())
                .familyHistory(n.getFamilyHistory())
                .nutritionalHistory(n.getNutritionalHistory())
                .developmentalHistory(n.getDevelopmentalHistory())
                .examinationFindings(n.getExaminationFindings())
                .provisionalDiagnosis(n.getProvisionalDiagnosis())
                .investigationsOrdered(n.getInvestigationsOrdered())
                .treatmentPlan(n.getTreatmentPlan())
                .patientFamilyEducation(n.getPatientFamilyEducation())
                .followUpPlan(n.getFollowUpPlan())
                .admitTo(n.getAdmitTo())
                .isDraft(n.getIsDraft())
                .createdDate(n.getCreatedDate())
                .updatedDate(n.getUpdatedDate())
                .build();
    }
}
