package com.lockdoc.outpatient.service;

import com.lockdoc.common.config.SecurityUtils;
import com.lockdoc.outpatient.dto.BillResponse;
import com.lockdoc.outpatient.dto.PatientRegistrationRequest;
import com.lockdoc.outpatient.dto.PatientRegistrationResponse;
import com.lockdoc.common.entity.Facility;
import com.lockdoc.outpatient.entity.Bill;
import com.lockdoc.outpatient.entity.PatientRegistration;
import com.lockdoc.outpatient.entity.RegistrationFeeConfig;
import com.lockdoc.outpatient.entity.Patient;
import com.lockdoc.common.exception.InvalidDocumentStateException;
import com.lockdoc.common.exception.ResourceNotFoundException;
import com.lockdoc.common.repository.FacilityRepository;
import com.lockdoc.outpatient.repository.PatientRegistrationRepository;
import com.lockdoc.outpatient.repository.RegistrationFeeConfigRepository;
import com.lockdoc.outpatient.repository.PatientRepository;
import com.lockdoc.common.service.DocumentNumberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Registration/re-registration (Master Spec §7.4) - a billable
 * transaction. First registration bills {@code firstFee}; every
 * subsequent one bills {@code reRegistrationFee}, regardless of whether
 * the prior one has actually expired yet (front desk decides whether a
 * re-registration is needed; this service just prices whichever one it's
 * asked to create).
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PatientRegistrationService {

    private static final String DOC_TYPE = "REGISTRATION";
    private static final String PREFIX = "REG";

    private final PatientRegistrationRepository registrationRepository;
    private final RegistrationFeeConfigRepository feeConfigRepository;
    private final PatientRepository patientRepository;
    private final FacilityRepository facilityRepository;
    private final BillingService billingService;
    private final DocumentNumberService documentNumberService;

    public PatientRegistrationResponse register(PatientRegistrationRequest request) {
        Long facilityId = SecurityUtils.requireFacilityId();
        Long userId = SecurityUtils.currentUserId();

        RegistrationFeeConfig feeConfig = feeConfigRepository.findByFacilityId(facilityId)
                .orElseThrow(() -> new InvalidDocumentStateException(
                        "This facility has not configured its registration fee yet - a Hospital/Clinic Admin must set it first"));

        Patient patient = patientRepository.findByIdAndFacilityId(request.getPatientId(), facilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + request.getPatientId()));

        List<PatientRegistration> prior = registrationRepository.findByPatientIdAndFacilityIdOrderByRegisteredAtDesc(patient.getId(), facilityId);
        boolean isReRegistration = !prior.isEmpty();

        Facility facility = facilityRepository.getReferenceById(facilityId);
        PatientRegistration registration = PatientRegistration.builder()
                .patient(patient)
                .facility(facility)
                .registrationNo(documentNumberService.next(facilityId, DOC_TYPE, PREFIX))
                .isReRegistration(isReRegistration)
                .expiryDate(feeConfig.getValidityDays() == null ? null : LocalDate.now().plusDays(feeConfig.getValidityDays()))
                .registeredAt(LocalDateTime.now())
                .registeredByUserId(userId)
                .build();
        registration = registrationRepository.save(registration);

        var fee = isReRegistration ? feeConfig.getReRegistrationFee() : feeConfig.getFirstFee();
        Bill bill = billingService.createBill(facility, patient, Bill.ENCOUNTER_REGISTRATION, registration.getId(), fee, userId);

        return PatientRegistrationResponse.toResponse(registration, BillResponse.toResponse(bill));
    }

    /** The most recent registration for this patient at this facility - used to warn at lookup (§7.4) if expired/expiring. */
    public PatientRegistrationResponse latestForPatient(Long patientId) {
        Long facilityId = SecurityUtils.requireFacilityId();
        return registrationRepository.findByPatientIdAndFacilityIdOrderByRegisteredAtDesc(patientId, facilityId).stream()
                .findFirst()
                .map(r -> {
                    BillResponse bill = billingService.getByEncounter(Bill.ENCOUNTER_REGISTRATION, r.getId());
                    return PatientRegistrationResponse.toResponse(r, bill);
                })
                .orElse(null);
    }
}
