package com.lockdoc.app.entity.doctor;

import com.lockdoc.app.entity.Doctor;
import com.lockdoc.app.entity.Facility;
import com.lockdoc.app.entity.op.OpVisit;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * The full clinical field set off the real OP card (Master Spec §6/§8.7)
 * — genuinely clinical documentation, not purely administrative (§7.1's
 * reception/doctor split: this belongs to the Doctor Module, never to
 * reception's screens). One row per {@link OpVisit}, draft until the
 * doctor explicitly completes the consultation — a draft is never the
 * visit's final record (§8.7's own requirement).
 */
@Entity
@Table(name = "consultation_notes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "op_visit_id", nullable = false, unique = true)
    private OpVisit opVisit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id", nullable = false)
    private Facility facility;

    @Column(name = "chief_complaint", length = 2000)
    private String chiefComplaint;

    @Column(name = "past_history", length = 2000)
    private String pastHistory;

    @Column(name = "family_history", length = 2000)
    private String familyHistory;

    @Column(name = "nutritional_history", length = 2000)
    private String nutritionalHistory;

    @Column(name = "developmental_history", length = 2000)
    private String developmentalHistory;

    @Column(name = "examination_findings", length = 2000)
    private String examinationFindings;

    @Column(name = "provisional_diagnosis", length = 2000)
    private String provisionalDiagnosis;

    @Column(name = "investigations_ordered", length = 2000)
    private String investigationsOrdered;

    @Column(name = "treatment_plan", length = 2000)
    private String treatmentPlan;

    @Column(name = "patient_family_education", length = 2000)
    private String patientFamilyEducation;

    @Column(name = "follow_up_plan", length = 2000)
    private String followUpPlan;

    @Column(name = "admit_to", length = 200)
    private String admitTo;

    @Column(name = "is_draft", nullable = false)
    @Builder.Default
    private Boolean isDraft = true;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date", nullable = false)
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
