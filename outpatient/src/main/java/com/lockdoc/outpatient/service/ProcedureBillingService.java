package com.lockdoc.outpatient.service;

import com.lockdoc.common.config.SecurityUtils;
import com.lockdoc.outpatient.dto.ProcedureBillRequest;
import com.lockdoc.outpatient.dto.ProcedureBillResponse;
import com.lockdoc.common.entity.Doctor;
import com.lockdoc.common.entity.Facility;
import com.lockdoc.outpatient.entity.Bill;
import com.lockdoc.outpatient.entity.BillableItem;
import com.lockdoc.outpatient.entity.ProcedureBill;
import com.lockdoc.outpatient.entity.Patient;
import com.lockdoc.common.exception.InvalidDocumentStateException;
import com.lockdoc.common.exception.ResourceNotFoundException;
import com.lockdoc.common.repository.DoctorRepository;
import com.lockdoc.common.repository.FacilityRepository;
import com.lockdoc.outpatient.repository.BillableItemRepository;
import com.lockdoc.outpatient.repository.BillRepository;
import com.lockdoc.outpatient.repository.ProcedureBillRepository;
import com.lockdoc.outpatient.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Procedure billing (Master Spec §7.5) - the third billable OP encounter,
 * routed through {@link BillingService#createBill} the same as
 * registration and consultation so gross/discount/net/paid/due stays one
 * source of truth. Two patient paths (V44): a registered patient by id,
 * or an unregistered walk-in captured inline (name required, age/gender/
 * mobile optional) with no {@code patient_id} at all - exactly one of
 * the two, enforced here rather than at the schema level, since which
 * one is valid depends on which fields the caller actually sent.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ProcedureBillingService {

    private static final List<String> VALID_ORG_TYPES =
            List.of(BillableItem.ORG_TYPE_DIRECT, BillableItem.ORG_TYPE_ORGANIZATION, BillableItem.ORG_TYPE_TPA);

    private final ProcedureBillRepository procedureBillRepository;
    private final BillRepository billRepository;
    private final BillableItemRepository billableItemRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final FacilityRepository facilityRepository;
    private final BillingService billingService;

    public ProcedureBillResponse bill(ProcedureBillRequest request) {
        Long facilityId = SecurityUtils.requireFacilityId();
        Long userId = SecurityUtils.currentUserId();

        boolean hasPatientId = request.getPatientId() != null;
        boolean hasWalkIn = StringUtils.hasText(request.getWalkInName());
        if (hasPatientId == hasWalkIn) {
            throw new InvalidDocumentStateException(
                    hasPatientId
                            ? "Provide either patientId or walkInName, not both"
                            : "Provide either a registered patientId or a walkInName for an unregistered walk-in");
        }
        if (request.getBillableItemId() == null) {
            throw new InvalidDocumentStateException("billableItemId is required");
        }

        Patient patient = null;
        if (hasPatientId) {
            patient = patientRepository.findByIdAndFacilityId(request.getPatientId(), facilityId)
                    .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + request.getPatientId()));
        }

        if (patient != null) {
            requireConsultationSettled(patient, facilityId);
        }

        BillableItem item = billableItemRepository.findByIdAndFacilityId(request.getBillableItemId(), facilityId)
                .filter(BillableItem::getActive)
                .orElseThrow(() -> new ResourceNotFoundException("Billable item not found with id: " + request.getBillableItemId()));

        String orgType = StringUtils.hasText(request.getOrgType()) ? request.getOrgType() : BillableItem.ORG_TYPE_DIRECT;
        if (!VALID_ORG_TYPES.contains(orgType)) {
            throw new InvalidDocumentStateException("Unknown orgType: " + orgType + " - expected one of " + VALID_ORG_TYPES);
        }

        Doctor doctor = null;
        if (request.getDoctorId() != null) {
            doctor = doctorRepository.findById(request.getDoctorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + request.getDoctorId()));
        }

        Facility facility = facilityRepository.getReferenceById(facilityId);
        ProcedureBill procedureBill = ProcedureBill.builder()
                .facility(facility)
                .patient(patient)
                .walkInName(hasWalkIn ? request.getWalkInName() : null)
                .walkInAge(hasWalkIn ? request.getWalkInAge() : null)
                .walkInGender(hasWalkIn ? request.getWalkInGender() : null)
                .walkInMobile(hasWalkIn ? request.getWalkInMobile() : null)
                .billableItem(item)
                .doctor(doctor)
                .orgType(orgType)
                .performedByName(request.getPerformedByName())
                .createdByUserId(userId)
                .build();
        procedureBill = procedureBillRepository.save(procedureBill);

        Bill bill = billingService.createBill(facility, patient, Bill.ENCOUNTER_PROCEDURE, procedureBill.getId(), item.rateFor(orgType), userId);
        procedureBill.setBillId(bill.getId());
        procedureBill = procedureBillRepository.save(procedureBill);

        return ProcedureBillResponse.toResponse(procedureBill, bill.getBillNo());
    }

    public List<ProcedureBillResponse> listForPatient(Long patientId) {
        Long facilityId = SecurityUtils.requireFacilityId();
        patientRepository.findByIdAndFacilityId(patientId, facilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + patientId));
        return procedureBillRepository.findByPatientIdOrderByCreatedDateDesc(patientId).stream()
                .map(p -> ProcedureBillResponse.toResponse(p, null))
                .toList();
    }

    /**
     * No procedure while a consultation is still owed (Master Spec §7.5).
     *
     * The doctor's fee is raised when the patient arrives and collected at the
     * counter; a procedure booked on top of an unpaid one is how a patient walks
     * out owing two bills nobody asked them for. So the procedure waits: the desk
     * collects in Billing &amp; Collections, which takes seconds, and then bills it.
     *
     * Only the registered-patient path is checked — an unregistered walk-in
     * (§7.5's own case) has no consultation history to owe against.
     */
    private void requireConsultationSettled(Patient patient, Long facilityId) {
        billRepository.findByPatientIdAndFacilityIdOrderByCreatedDateDesc(patient.getId(), facilityId).stream()
                .filter(b -> Bill.ENCOUNTER_CONSULTATION.equals(b.getEncounterType()))
                .filter(b -> b.getDue() != null && b.getDue().compareTo(java.math.BigDecimal.ZERO) > 0)
                .filter(b -> !Bill.STATUS_CANCELLED.equals(b.getStatus()))
                .findFirst()
                .ifPresent(unpaid -> {
                    throw new InvalidDocumentStateException(
                            patient.getFullName() + " still owes " + unpaid.getDue() + " on consultation bill " + unpaid.getBillNo()
                                    + " - collect it in Billing & Collections before billing a procedure");
                });
    }
}
