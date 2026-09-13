package com.lockdoc.outpatient.service.dataprotection;

import com.lockdoc.common.config.SecurityUtils;
import com.lockdoc.outpatient.dto.dataprotection.ErasureRequestCreateRequest;
import com.lockdoc.outpatient.dto.dataprotection.ErasureRequestResolveRequest;
import com.lockdoc.outpatient.dto.dataprotection.ErasureRequestResponse;
import com.lockdoc.common.entity.Facility;
import com.lockdoc.outpatient.entity.dataprotection.ErasureRequest;
import com.lockdoc.outpatient.entity.Patient;
import com.lockdoc.common.exception.InvalidDocumentStateException;
import com.lockdoc.common.exception.ResourceNotFoundException;
import com.lockdoc.common.repository.FacilityRepository;
import com.lockdoc.outpatient.repository.dataprotection.ErasureRequestRepository;
import com.lockdoc.outpatient.repository.OpVisitRepository;
import com.lockdoc.outpatient.repository.PatientRepository;
import com.lockdoc.common.service.platform.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DPDP erasure/grievance queue (Master Spec §18.4/§18.6, screen #38b).
 * "No hard deletes" (§5 principle 3) and DPDP's erasure right are
 * reconciled, not in conflict: a patient with no billed history can be
 * genuinely erased; one with OP visits against them
 * can only be anonymized (non-essential PII cleared, the clinical/
 * financial record itself survives under its statutory retention).
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ErasureRequestService {

    private final ErasureRequestRepository erasureRequestRepository;
    private final PatientRepository patientRepository;
    private final FacilityRepository facilityRepository;
    private final OpVisitRepository opVisitRepository;
    private final AuditLogService auditLogService;

    public List<ErasureRequestResponse> list() {
        Long facilityId = SecurityUtils.requireFacilityId();
        return erasureRequestRepository.findByFacilityIdOrderByRequestedAtAsc(facilityId).stream()
                .map(ErasureRequestResponse::toResponse)
                .toList();
    }

    public ErasureRequestResponse create(ErasureRequestCreateRequest request) {
        Long facilityId = SecurityUtils.requireFacilityId();
        Patient patient = patientRepository.findByIdAndFacilityId(request.getPatientId(), facilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + request.getPatientId()));
        Facility facility = facilityRepository.getReferenceById(facilityId);

        ErasureRequest erasureRequest = ErasureRequest.builder()
                .facility(facility)
                .patient(patient)
                .patientNameSnapshot(patient.getFullName())
                .patientMrnSnapshot(patient.getMrn())
                .requestedVia(request.getRequestedVia())
                .requestedBy(request.getRequestedBy())
                .status(ErasureRequest.STATUS_RECEIVED)
                .build();
        return ErasureRequestResponse.toResponse(erasureRequestRepository.save(erasureRequest));
    }

    /** A human decides this - it's never automatic (§18.4). */
    public ErasureRequestResponse resolve(Long id, ErasureRequestResolveRequest request) {
        Long facilityId = SecurityUtils.requireFacilityId();
        ErasureRequest erasureRequest = erasureRequestRepository.findByIdAndFacilityId(id, facilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Erasure request not found with id: " + id));
        if (ErasureRequest.STATUS_FULFILLED_ANONYMIZED.equals(erasureRequest.getStatus())
                || ErasureRequest.STATUS_FULFILLED_DELETED.equals(erasureRequest.getStatus())) {
            throw new InvalidDocumentStateException("This request is already resolved (" + erasureRequest.getStatus() + ")");
        }

        String statusBefore = erasureRequest.getStatus();
        Patient patient = erasureRequest.getPatient();

        if (ErasureRequest.STATUS_FULFILLED_DELETED.equals(request.getStatus())) {
            boolean hasHistory = opVisitRepository.existsByPatientId(patient.getId());
            if (hasHistory) {
                throw new InvalidDocumentStateException(
                        "This patient has billed OP visits on record - statutory retention applies. Use \"Fulfilled (anonymized)\" instead of a hard delete.");
            }
            patientRepository.delete(patient);
            patientRepository.flush();
            erasureRequest.setPatient(null);
        } else if (ErasureRequest.STATUS_FULFILLED_ANONYMIZED.equals(request.getStatus())) {
            anonymize(patient);
        }

        boolean isTerminal = !ErasureRequest.STATUS_UNDER_REVIEW.equals(request.getStatus())
                && !ErasureRequest.STATUS_RECEIVED.equals(request.getStatus());

        erasureRequest.setStatus(request.getStatus());
        erasureRequest.setReviewedByUserId(SecurityUtils.currentUserId());
        if (isTerminal) {
            erasureRequest.setResolvedAt(LocalDateTime.now());
        }
        erasureRequest.setResolutionNotes(request.getResolutionNotes());
        ErasureRequestResponse response = ErasureRequestResponse.toResponse(erasureRequestRepository.save(erasureRequest));

        auditLogService.record(facilityId, SecurityUtils.currentUserId(), "ERASURE_REQUEST_RESOLVE", "ErasureRequest", id,
                "status=" + statusBefore, "status=" + request.getStatus());
        return response;
    }

    /** Non-essential PII cleared (address, free-text allergies/history, phone) - clinical/financial identity (name, MRN, DOB, gender) survives for statutory retention (§18.4). */
    private void anonymize(Patient patient) {
        patient.setAddress("[erased on request]");
        patient.setAllergies("[erased on request]");
        patient.setPhone(null);
        patientRepository.save(patient);
    }
}
