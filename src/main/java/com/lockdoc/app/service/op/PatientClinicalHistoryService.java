package com.lockdoc.app.service.op;

import com.lockdoc.app.config.SecurityUtils;
import com.lockdoc.app.dto.doctor.ConsultationNoteResponse;
import com.lockdoc.app.dto.doctor.PrescriptionLineResponse;
import com.lockdoc.app.dto.op.PatientClinicalVisitResponse;
import com.lockdoc.app.entity.doctor.ConsultationNote;
import com.lockdoc.app.entity.doctor.Prescription;
import com.lockdoc.app.entity.op.OpVisit;
import com.lockdoc.app.repository.doctor.ConsultationNoteRepository;
import com.lockdoc.app.repository.doctor.PrescriptionRepository;
import com.lockdoc.app.repository.op.OpVisitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * What happened in the room, for one patient, at this facility (Master Spec §7.4/§8.7).
 *
 * Scoped to the caller's facility exactly like every other OP read: a visit at another
 * clinic is not this counter's business, even for the same patient. Draft notes and draft
 * prescriptions are dropped rather than shown half-written - see
 * {@link PatientClinicalVisitResponse} for why.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PatientClinicalHistoryService {

    private final OpVisitRepository visitRepository;
    private final ConsultationNoteRepository noteRepository;
    private final PrescriptionRepository prescriptionRepository;

    public List<PatientClinicalVisitResponse> forPatient(Long patientId) {
        Long facilityId = SecurityUtils.requireFacilityId();
        return visitRepository.findByPatientIdAndFacilityIdOrderByArrivedTsDesc(patientId, facilityId)
                .stream()
                .map(this::toResponse)
                .filter(v -> v.getNote() != null || !v.getPrescribed().isEmpty())
                .toList();
    }

    private PatientClinicalVisitResponse toResponse(OpVisit visit) {
        ConsultationNote note = noteRepository.findByOpVisitId(visit.getId())
                .filter(n -> !Boolean.TRUE.equals(n.getIsDraft()))
                .orElse(null);
        List<PrescriptionLineResponse> lines = prescriptionRepository.findByOpVisitId(visit.getId())
                .filter(p -> !Boolean.TRUE.equals(p.getIsDraft()))
                .map(Prescription::getLines)
                .orElse(List.of())
                .stream()
                .map(PrescriptionLineResponse::toResponse)
                .toList();

        return PatientClinicalVisitResponse.builder()
                .opVisitId(visit.getId())
                .opNo(visit.getOpNo())
                .visitDate(visit.getArrivedTs())
                .visitType(visit.getVisitType())
                .doctorName(visit.getDoctor().getFullName())
                .doctorSpecialties(visit.getDoctor().getSpecialties())
                .doctorRegistrationNumber(visit.getDoctor().getRegistrationNumber())
                .weightKg(visit.getWeightKg())
                .heightCm(visit.getHeightCm())
                .temperatureF(visit.getTemperatureF())
                .bp(visit.getBp())
                .note(note != null ? ConsultationNoteResponse.toResponse(note) : null)
                .prescribed(lines)
                .build();
    }
}
