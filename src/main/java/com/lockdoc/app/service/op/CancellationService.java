package com.lockdoc.app.service.op;

import com.lockdoc.app.config.SecurityUtils;
import com.lockdoc.app.dto.op.CancellationRequest;
import com.lockdoc.app.dto.op.CancellationResponse;
import com.lockdoc.app.entity.Facility;
import com.lockdoc.app.entity.op.Appointment;
import com.lockdoc.app.entity.op.Bill;
import com.lockdoc.app.entity.op.Cancellation;
import com.lockdoc.app.entity.op.OpVisit;
import com.lockdoc.app.exception.InvalidDocumentStateException;
import com.lockdoc.app.exception.ResourceNotFoundException;
import com.lockdoc.app.repository.FacilityRepository;
import com.lockdoc.app.repository.op.AppointmentRepository;
import com.lockdoc.app.repository.op.BillRepository;
import com.lockdoc.app.repository.op.CancellationRepository;
import com.lockdoc.app.repository.op.OpVisitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Cancellation requests (Master Spec §6/§4.1/§7.5) - unconditionally
 * Pending Approval when a Receptionist requests one ("request only", no
 * threshold - see V23's migration comment for why this differs from
 * Discount's threshold-driven auto-approve). A Hospital/Clinic Admin's
 * own request auto-resolves approved, since they also hold the approve
 * right - there is no one else for them to ask.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CancellationService {

    private final CancellationRepository cancellationRepository;
    private final AppointmentRepository appointmentRepository;
    private final OpVisitRepository opVisitRepository;
    private final BillRepository billRepository;
    private final BillingService billingService;
    private final FacilityRepository facilityRepository;

    public CancellationResponse request(String entityType, Long entityId, CancellationRequest request) {
        Long facilityId = SecurityUtils.requireFacilityId();
        Long userId = SecurityUtils.currentUserId();
        var principal = SecurityUtils.currentPrincipal();

        validateEntityBelongsToFacility(entityType, entityId, facilityId);

        boolean isHospitalAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> "ROLE_HOSPITAL_ADMIN".equals(a.getAuthority()));

        Facility facility = facilityRepository.getReferenceById(facilityId);
        Cancellation cancellation = Cancellation.builder()
                .facility(facility)
                .entityType(entityType)
                .entityId(entityId)
                .reason(request.getReason())
                .status(isHospitalAdmin ? Cancellation.STATUS_APPROVED : Cancellation.STATUS_PENDING_APPROVAL)
                .requestedByUserId(userId)
                .build();

        if (isHospitalAdmin) {
            cancellation.setApprovedByUserId(userId);
            cancellation.setApprovedAt(LocalDateTime.now());
            applyCancellation(entityType, entityId, request.getReason());
        }

        return CancellationResponse.toResponse(cancellationRepository.save(cancellation));
    }

    public CancellationResponse approve(Long id) {
        Long facilityId = SecurityUtils.requireFacilityId();
        Long userId = SecurityUtils.currentUserId();
        Cancellation cancellation = cancellationRepository.findByIdAndFacilityId(id, facilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Cancellation request not found with id: " + id));
        if (!Cancellation.STATUS_PENDING_APPROVAL.equals(cancellation.getStatus())) {
            throw new InvalidDocumentStateException("Cancellation can only be approved from PENDING_APPROVAL, current status: " + cancellation.getStatus());
        }
        cancellation.setStatus(Cancellation.STATUS_APPROVED);
        cancellation.setApprovedByUserId(userId);
        cancellation.setApprovedAt(LocalDateTime.now());
        applyCancellation(cancellation.getEntityType(), cancellation.getEntityId(), cancellation.getReason());
        return CancellationResponse.toResponse(cancellationRepository.save(cancellation));
    }

    public CancellationResponse reject(Long id) {
        Long facilityId = SecurityUtils.requireFacilityId();
        Long userId = SecurityUtils.currentUserId();
        Cancellation cancellation = cancellationRepository.findByIdAndFacilityId(id, facilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Cancellation request not found with id: " + id));
        if (!Cancellation.STATUS_PENDING_APPROVAL.equals(cancellation.getStatus())) {
            throw new InvalidDocumentStateException("Cancellation can only be rejected from PENDING_APPROVAL, current status: " + cancellation.getStatus());
        }
        cancellation.setStatus(Cancellation.STATUS_REJECTED);
        cancellation.setApprovedByUserId(userId);
        cancellation.setApprovedAt(LocalDateTime.now());
        return CancellationResponse.toResponse(cancellationRepository.save(cancellation));
    }

    public List<CancellationResponse> pending() {
        Long facilityId = SecurityUtils.requireFacilityId();
        return cancellationRepository.findByFacilityIdAndStatusOrderByCreatedDateAsc(facilityId, Cancellation.STATUS_PENDING_APPROVAL)
                .stream().map(c -> CancellationResponse.toResponse(c, subjectLabel(c))).toList();
    }

    /** "Anita Rao (PT-2026-000002)" for a visit cancellation, so reception's queue reads as a person, not a bare id. Null for other entity types (unchanged). */
    private String subjectLabel(Cancellation c) {
        if (!Cancellation.ENTITY_OP_VISIT.equals(c.getEntityType())) return null;
        return opVisitRepository.findById(c.getEntityId())
                .map(v -> v.getPatient().getFullName() + " (" + v.getPatient().getMrn() + ")")
                .orElse(null);
    }

    private void validateEntityBelongsToFacility(String entityType, Long entityId, Long facilityId) {
        if (Cancellation.ENTITY_APPOINTMENT.equals(entityType)) {
            appointmentRepository.findByIdAndFacilityId(entityId, facilityId)
                    .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + entityId));
        } else if (Cancellation.ENTITY_BILL.equals(entityType)) {
            billingService.findEntity(entityId); // throws if not found/not this facility
        } else if (Cancellation.ENTITY_OP_VISIT.equals(entityType)) {
            findVisit(entityId, facilityId);
        } else {
            throw new InvalidDocumentStateException("Unknown cancellation entity type: " + entityType);
        }
    }

    /**
     * Cancelling a visit also cancels its consultation bill, if one exists - same reasoning
     * as the plain ENTITY_BILL path, just reached from the doctor's own "Cancel
     * Consultation" action instead of Billing/Due Collection. A paid bill's money doesn't
     * move here; it only flags REFUND_PENDING, which the new Refunds screen surfaces to
     * reception (§5 principle 4 - no silent money movement, ever).
     */
    private void applyCancellation(String entityType, Long entityId, String reason) {
        if (Cancellation.ENTITY_APPOINTMENT.equals(entityType)) {
            Appointment appointment = appointmentRepository.findById(entityId)
                    .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + entityId));
            appointment.setStatus(Appointment.STATUS_CANCELLED);
            appointment.setCancelReason(reason);
            appointmentRepository.save(appointment);
        } else if (Cancellation.ENTITY_BILL.equals(entityType)) {
            Bill bill = billingService.findEntity(entityId);
            billingService.cancelBill(bill);
        } else if (Cancellation.ENTITY_OP_VISIT.equals(entityType)) {
            OpVisit visit = opVisitRepository.findById(entityId)
                    .orElseThrow(() -> new ResourceNotFoundException("OP visit not found with id: " + entityId));
            visit.setStatus(OpVisit.STATUS_CANCELLED);
            visit.setCancelReason(reason);
            opVisitRepository.save(visit);

            billRepository.findByEncounterTypeAndEncounterId(Bill.ENCOUNTER_CONSULTATION, visit.getId())
                    .filter(bill -> !Bill.STATUS_CANCELLED.equals(bill.getStatus()))
                    .ifPresent(billingService::cancelBill);
        }
    }

    private OpVisit findVisit(Long id, Long facilityId) {
        OpVisit visit = opVisitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OP visit not found with id: " + id));
        if (!visit.getFacility().getId().equals(facilityId)) {
            throw new ResourceNotFoundException("OP visit not found with id: " + id);
        }
        return visit;
    }
}
