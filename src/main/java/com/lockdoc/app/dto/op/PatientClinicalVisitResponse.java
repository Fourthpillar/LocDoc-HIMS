package com.lockdoc.app.dto.op;

import com.lockdoc.app.dto.doctor.ConsultationNoteResponse;
import com.lockdoc.app.dto.doctor.PrescriptionLineResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * One finished consultation, as the front desk needs to read it back.
 *
 * The clinical record already existed, but only behind {@code CONSULTATION_RECORD_MANAGE}
 * and only ever for the doctor who wrote it - so the patient's own record screen could
 * show when they came and what they paid, and nothing at all about what happened in the
 * room. Reception already prints this exact content on the OP card; withholding it from
 * the screen that prints it served nobody.
 *
 * Drafts are deliberately excluded: an unfinished note is the doctor's working copy, not
 * the visit's record (§8.7), and a half-written diagnosis read back at the counter is
 * worse than no diagnosis at all.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientClinicalVisitResponse {

    private Long opVisitId;
    private String opNo;
    private LocalDateTime visitDate;
    private String visitType;
    private String doctorName;
    private String doctorSpecialties;
    private String doctorRegistrationNumber;

    /** Vitals belong to the visit, not the note - they are recorded at check-in. */
    private java.math.BigDecimal weightKg;
    private java.math.BigDecimal heightCm;
    private java.math.BigDecimal temperatureF;
    private String bp;

    private ConsultationNoteResponse note;
    private List<PrescriptionLineResponse> prescribed;
}
