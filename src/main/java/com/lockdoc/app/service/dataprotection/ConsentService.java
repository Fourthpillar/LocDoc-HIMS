package com.lockdoc.app.service.dataprotection;

import com.lockdoc.app.config.SecurityUtils;
import com.lockdoc.app.dto.dataprotection.ConsentRequest;
import com.lockdoc.app.dto.dataprotection.ConsentResponse;
import com.lockdoc.app.entity.Facility;
import com.lockdoc.app.entity.dataprotection.Consent;
import com.lockdoc.app.entity.pharmacy.Patient;
import com.lockdoc.app.exception.ResourceNotFoundException;
import com.lockdoc.app.repository.FacilityRepository;
import com.lockdoc.app.repository.dataprotection.ConsentRepository;
import com.lockdoc.app.repository.pharmacy.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/** Consent captured, not assumed (Master Spec §18.2) - two separate consents, each versioned and auditable. */
@Service
@RequiredArgsConstructor
@Transactional
public class ConsentService {

    private final ConsentRepository consentRepository;
    private final PatientRepository patientRepository;
    private final FacilityRepository facilityRepository;

    /** Master Spec §18.2/§7.4 - Reception's in-app action; the checkbox submit itself IS the record. */
    public ConsentResponse capture(ConsentRequest request) {
        Long facilityId = SecurityUtils.requireFacilityId();
        Patient patient = patientRepository.findByIdAndFacilityId(request.getPatientId(), facilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + request.getPatientId()));
        Facility facility = facilityRepository.getReferenceById(facilityId);

        List<Consent> existing = consentRepository.findByFacilityIdAndPatientIdOrderByCapturedAtDesc(facilityId, patient.getId());
        int nextVersion = existing.stream()
                .filter(c -> c.getConsentType().equals(request.getConsentType()))
                .map(Consent::getVersion)
                .max(Integer::compareTo)
                .map(v -> v + 1)
                .orElse(1);

        Consent consent = Consent.builder()
                .facility(facility)
                .patient(patient)
                .patientNameSnapshot(patient.getFullName())
                .patientMrnSnapshot(patient.getMrn())
                .consentType(request.getConsentType())
                .version(nextVersion)
                .textShown(request.getTextShown())
                .capturedByUserId(SecurityUtils.currentUserId())
                .method(StringUtils.hasText(request.getMethod()) ? request.getMethod() : Consent.METHOD_IN_APP_CHECKBOX)
                .guardianName(request.getGuardianName())
                .guardianRelation(request.getGuardianRelation())
                .build();
        return ConsentResponse.toResponse(consentRepository.save(consent));
    }

    /** Master Spec §17.7 #38a - per-patient consent history, searchable by patient/UHID (search happens at the patient-lookup step, not here). */
    public List<ConsentResponse> historyForPatient(Long patientId) {
        Long facilityId = SecurityUtils.requireFacilityId();
        return consentRepository.findByFacilityIdAndPatientIdOrderByCapturedAtDesc(facilityId, patientId).stream()
                .map(ConsentResponse::toResponse)
                .toList();
    }
}
