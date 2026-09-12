package com.lockdoc.app.service.op;

import com.lockdoc.app.config.SecurityUtils;
import com.lockdoc.app.dto.op.BillResponse;
import com.lockdoc.app.dto.op.PatientRegistrationRequest;
import com.lockdoc.app.dto.op.PatientRegistrationResponse;
import com.lockdoc.app.entity.Facility;
import com.lockdoc.app.entity.op.Bill;
import com.lockdoc.app.entity.op.PatientRegistration;
import com.lockdoc.app.entity.op.RegistrationFeeConfig;
import com.lockdoc.app.entity.pharmacy.Patient;
import com.lockdoc.app.exception.InvalidDocumentStateException;
import com.lockdoc.app.exception.ResourceNotFoundException;
import com.lockdoc.app.repository.FacilityRepository;
import com.lockdoc.app.repository.op.PatientRegistrationRepository;
import com.lockdoc.app.repository.op.RegistrationFeeConfigRepository;
import com.lockdoc.app.repository.pharmacy.PatientRepository;
import com.lockdoc.app.service.pharmacy.DocumentNumberService;
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
