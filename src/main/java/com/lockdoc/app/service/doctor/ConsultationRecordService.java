package com.lockdoc.app.service.doctor;

import com.lockdoc.app.config.SecurityUtils;
import com.lockdoc.app.dto.doctor.*;
import com.lockdoc.app.dto.op.CancellationResponse;
import com.lockdoc.app.dto.pharmacy.MedicineStockResponse;
import com.lockdoc.app.entity.Doctor;
import com.lockdoc.app.entity.doctor.ConsultationNote;
import com.lockdoc.app.entity.doctor.Prescription;
import com.lockdoc.app.entity.doctor.PrescriptionLine;
import com.lockdoc.app.entity.op.Bill;
import com.lockdoc.app.entity.op.Cancellation;
import com.lockdoc.app.entity.op.OpVisit;
import com.lockdoc.app.entity.op.PatientRegistration;
import com.lockdoc.app.entity.pharmacy.Medicine;
import com.lockdoc.app.entity.pharmacy.Patient;
import com.lockdoc.app.exception.InvalidDocumentStateException;
import com.lockdoc.app.exception.ResourceNotFoundException;
import com.lockdoc.app.repository.DoctorRepository;
import com.lockdoc.app.repository.doctor.ConsultationNoteRepository;
import com.lockdoc.app.repository.doctor.PrescriptionRepository;
import com.lockdoc.app.repository.op.BillRepository;
import com.lockdoc.app.repository.op.CancellationRepository;
import com.lockdoc.app.repository.op.OpVisitRepository;
import com.lockdoc.app.repository.op.PatientRegistrationRepository;
import com.lockdoc.app.repository.pharmacy.MedicineBatchRepository;
import com.lockdoc.app.repository.pharmacy.MedicineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.Period;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Consultation Workspace's backend (Master Spec §8.7, build order
 * step 6) — the biggest single screen in this document, and the pass
 * that finally wires the Doctor Module to Pharmacy's already-built stock
 * data. Deliberately non-facility-scoped throughout (a Doctor's JWT
 * carries no facilityId, §4) — every method instead verifies the OP
 * visit in question actually belongs to the calling doctor.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ConsultationRecordService {

    private final OpVisitRepository opVisitRepository;
    private final ConsultationNoteRepository noteRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final DoctorRepository doctorRepository;
    private final MedicineRepository medicineRepository;
    private final MedicineBatchRepository medicineBatchRepository;
    private final PatientRegistrationRepository registrationRepository;
    private final BillRepository billRepository;
    private final CancellationRepository cancellationRepository;

    // ---- Queue ----------------------------------------------------

    /** Today's visits waiting on the current doctor, across every facility they practise at (§17.6). */
    public List<ConsultationQueueEntry> myQueueToday() {
        Doctor doctor = currentDoctor();
        LocalDate today = LocalDate.now();
        List<OpVisit> visits = opVisitRepository.findByDoctorIdAndArrivedTsBetweenOrderByArrivedTsAsc(
                doctor.getId(), today.atStartOfDay(), today.plusDays(1).atStartOfDay());

        return visits.stream()
                .filter(v -> OpVisit.STATUS_ARRIVED.equals(v.getStatus()) || OpVisit.STATUS_IN_CONSULTATION.equals(v.getStatus()))
                .map(v -> ConsultationQueueEntry.toEntry(v, noteRepository.findByOpVisitId(v.getId()).map(ConsultationNote::getIsDraft).orElse(false)))
                .toList();
    }

    /** Prior, completed visits with this doctor for the same patient (§8.7 — opens collapsed by default on the frontend). */
    public List<ConsultationHistoryEntry> priorHistory(Long patientId) {
        Doctor doctor = currentDoctor();
        return noteRepository.findCompletedHistory(doctor.getId(), patientId).stream()
                .map(ConsultationHistoryEntry::toEntry)
                .toList();
    }

    /**
     * The printed OP card (§7.4/§9) for one visit — callable any time, not just after
     * completion, so reception's registration-only fields are still printable before a
     * consultation happens; {@code isDraft} tells the frontend whether to watermark it.
     */
    public OpCardResponse opCard(Long opVisitId) {
        return buildOpCard(ownedVisit(opVisitId));
    }

    /**
     * The same printed card, for the front desk (Master Spec 7.4).
     *
     * Reception is who physically hands this over, and before the doctor has written
     * anything it is the registration slip - the reason opCard was always callable on an
     * unfinished visit. The doctor-scoped entry point above could not serve them: it
     * resolves the visit by "is this yours", and a receptionist owns no visits. This one
     * resolves it by facility instead, which is the scope every other OP read already uses.
     */
    public OpCardResponse opCardForFacility(Long opVisitId) {
        Long facilityId = SecurityUtils.requireFacilityId();
        OpVisit visit = opVisitRepository.findByIdAndFacilityId(opVisitId, facilityId)
                .orElseThrow(() -> new ResourceNotFoundException("OP visit not found with id: " + opVisitId));
        return buildOpCard(visit);
    }

    private OpCardResponse buildOpCard(OpVisit visit) {
        Long opVisitId = visit.getId();
        Patient patient = visit.getPatient();

        PatientRegistration registration = registrationRepository
                .findByPatientIdAndFacilityIdOrderByRegisteredAtDesc(patient.getId(), visit.getFacility().getId())
                .stream().findFirst().orElse(null);
        Bill bill = billRepository.findByEncounterTypeAndEncounterId("CONSULTATION", visit.getId()).orElse(null);

        ConsultationNote note = noteRepository.findByOpVisitId(opVisitId).orElse(null);
        Prescription prescription = prescriptionRepository.findByOpVisitId(opVisitId).orElse(null);
        boolean noteDraft = note == null || Boolean.TRUE.equals(note.getIsDraft());
        boolean prescriptionDraft = prescription == null || Boolean.TRUE.equals(prescription.getIsDraft());

        return OpCardResponse.builder()
                .opVisitId(visit.getId())
                .opNo(visit.getOpNo())
                .visitType(visit.getVisitType())
                .mlcFlag(visit.getMlcFlag())
                .arrivedTs(visit.getArrivedTs())
                .patientName(patient.getFullName())
                .patientMrn(patient.getMrn())
                .patientDob(patient.getDateOfBirth())
                .patientAge(patient.getDateOfBirth() != null ? Period.between(patient.getDateOfBirth(), LocalDate.now()).getYears() : null)
                .patientGender(patient.getGender())
                .attendantName(visit.getAttendantName())
                .attendantMobile(visit.getAttendantMobile())
                .attendantRelation(visit.getAttendantRelation())
                .facilityName(visit.getFacility().getName())
                .doctorName(visit.getDoctor().getFullName())
                .doctorRegistrationNumber(visit.getDoctor().getRegistrationNumber())
                .doctorSpecialties(visit.getDoctor().getSpecialties())
                .registrationNo(registration != null ? registration.getRegistrationNo() : null)
                .registrationExpiryDate(registration != null ? registration.getExpiryDate() : null)
                .billNo(bill != null ? bill.getBillNo() : null)
                .billDate(bill != null ? bill.getCreatedDate() : null)
                .billAmount(bill != null ? bill.getNet() : null)
                .payorType(visit.getOrgType())
                .weightKg(visit.getWeightKg())
                .heightCm(visit.getHeightCm())
                .temperatureF(visit.getTemperatureF())
                .bp(visit.getBp())
                .note(note != null ? ConsultationNoteResponse.toResponse(note) : null)
                .prescription(prescription != null ? PrescriptionResponse.toResponse(prescription, resolveStockChips(prescription)) : null)
                .isDraft(noteDraft || prescriptionDraft)
                .build();
    }

    /**
     * "Cancel Consultation" (§5 principle 4, §7.5) - the gap this closes: a doctor could
     * already document or complete a visit, but had no way to ever cancel one (a patient who
     * leaves, a duplicate check-in, a doctor who can't see them after all). Always Pending
     * Approval - a doctor never holds OP_CANCELLATION_APPROVE, so this can't auto-resolve the
     * way a Hospital/Clinic Admin's own request does. Inserts the {@link Cancellation} row
     * directly rather than going through CancellationService.request(), which calls
     * SecurityUtils.requireFacilityId() - a doctor's JWT carries none (§4); the visit's own
     * facility is unambiguous here regardless, so there's nothing that check would add.
     * Reception sees it appear in their existing pending-cancellations queue with zero
     * changes needed there - same entity, same approval flow as an appointment or bill.
     */
    public CancellationResponse cancelVisit(Long opVisitId, String reason) {
        OpVisit visit = ownedVisit(opVisitId);
        if (OpVisit.STATUS_COMPLETED.equals(visit.getStatus()) || OpVisit.STATUS_CANCELLED.equals(visit.getStatus())) {
            throw new InvalidDocumentStateException("This visit is already " + visit.getStatus().toLowerCase() + " - it can't be cancelled");
        }
        if (!StringUtils.hasText(reason)) {
            throw new InvalidDocumentStateException("A reason is required to cancel a consultation");
        }
        if (cancellationRepository.findByEntityTypeAndEntityIdAndStatus(Cancellation.ENTITY_OP_VISIT, visit.getId(), Cancellation.STATUS_PENDING_APPROVAL).isPresent()) {
            throw new InvalidDocumentStateException("A cancellation for this visit is already pending approval");
        }

        Cancellation cancellation = Cancellation.builder()
                .facility(visit.getFacility())
                .entityType(Cancellation.ENTITY_OP_VISIT)
                .entityId(visit.getId())
                .reason(reason)
                .status(Cancellation.STATUS_PENDING_APPROVAL)
                .requestedByUserId(SecurityUtils.currentUserId())
                .build();
        return CancellationResponse.toResponse(cancellationRepository.save(cancellation));
    }

    // ---- Consultation note -----------------------------------------

    public ConsultationNoteResponse getNote(Long opVisitId) {
        return noteRepository.findByOpVisitId(opVisitId).map(ConsultationNoteResponse::toResponse).orElse(null);
    }

    /** Draft (or re-draft) save — the frontend calls this every ~15s and on blur (§8.7). */
    public ConsultationNoteResponse saveNoteDraft(Long opVisitId, ConsultationNoteRequest request) {
        OpVisit visit = ownedVisit(opVisitId);
        ConsultationNote note = noteRepository.findByOpVisitId(opVisitId)
                .orElseGet(() -> ConsultationNote.builder().opVisit(visit).doctor(visit.getDoctor()).facility(visit.getFacility()).isDraft(true).build());

        note.setChiefComplaint(request.getChiefComplaint());
        note.setPastHistory(request.getPastHistory());
        note.setFamilyHistory(request.getFamilyHistory());
        note.setNutritionalHistory(request.getNutritionalHistory());
        note.setDevelopmentalHistory(request.getDevelopmentalHistory());
        note.setExaminationFindings(request.getExaminationFindings());
        note.setProvisionalDiagnosis(request.getProvisionalDiagnosis());
        note.setInvestigationsOrdered(request.getInvestigationsOrdered());
        note.setTreatmentPlan(request.getTreatmentPlan());
        note.setPatientFamilyEducation(request.getPatientFamilyEducation());
        note.setFollowUpPlan(request.getFollowUpPlan());
        note.setAdmitTo(request.getAdmitTo());
        // isDraft stays true here even if it was already completed - completing
        // is its own explicit action (completeNote), never a side effect of a save.

        return ConsultationNoteResponse.toResponse(noteRepository.save(note));
    }

    /** A draft is never the visit's final record until explicitly completed (§8.7). */
    public ConsultationNoteResponse completeNote(Long opVisitId) {
        ownedVisit(opVisitId);
        ConsultationNote note = noteRepository.findByOpVisitId(opVisitId)
                .orElseThrow(() -> new ResourceNotFoundException("No consultation note drafted yet for this visit"));
        note.setIsDraft(false);
        return ConsultationNoteResponse.toResponse(noteRepository.save(note));
    }

    // ---- Prescription -----------------------------------------------

    public PrescriptionResponse getPrescription(Long opVisitId) {
        return prescriptionRepository.findByOpVisitId(opVisitId)
                .map(p -> PrescriptionResponse.toResponse(p, resolveStockChips(p)))
                .orElse(null);
    }

    /** Replaces every line wholesale - simplest correct model for a form saved as a whole (§8.7). */
    public PrescriptionResponse savePrescriptionDraft(Long opVisitId, PrescriptionRequest request) {
        OpVisit visit = ownedVisit(opVisitId);
        Prescription prescription = prescriptionRepository.findByOpVisitId(opVisitId)
                .orElseGet(() -> Prescription.builder().opVisit(visit).doctor(visit.getDoctor()).facility(visit.getFacility()).isDraft(true).build());

        prescription.getLines().clear();
        int order = 0;
        for (var lineRequest : request.getLines()) {
            prescription.getLines().add(PrescriptionLine.builder()
                    .prescription(prescription)
                    .lineOrder(order++)
                    .medicineName(lineRequest.getMedicineName())
                    .genericName(lineRequest.getGenericName())
                    .strength(lineRequest.getStrength())
                    .dosage(lineRequest.getDosage())
                    .route(lineRequest.getRoute())
                    .frequency(lineRequest.getFrequency())
                    .duration(lineRequest.getDuration())
                    .quantity(lineRequest.getQuantity())
                    .refillFlag(lineRequest.getRefillFlag() != null && lineRequest.getRefillFlag())
                    .matchedPharmacyMedicineId(lineRequest.getMatchedPharmacyMedicineId())
                    .build());
        }

        prescription = prescriptionRepository.save(prescription);
        return PrescriptionResponse.toResponse(prescription, resolveStockChips(prescription));
    }

    /**
     * Completing the prescription is the last step of the frontend's own
     * completion sequence (note, then prescription) - so this is also
     * where the OP visit itself transitions to COMPLETED. Without this,
     * a fully-documented consultation stayed ARRIVED/IN_CONSULTATION
     * forever and never left the doctor's own queue (myQueueToday()
     * filters on OpVisit.status, not on note/prescription draft state) -
     * a real gap caught live, not a hypothetical one.
     */
    public PrescriptionResponse completePrescription(Long opVisitId) {
        OpVisit visit = ownedVisit(opVisitId);
        Prescription prescription = prescriptionRepository.findByOpVisitId(opVisitId)
                .orElseThrow(() -> new ResourceNotFoundException("No prescription drafted yet for this visit"));
        prescription.setIsDraft(false);
        prescription = prescriptionRepository.save(prescription);

        visit.setStatus(OpVisit.STATUS_COMPLETED);
        visit.setConsultEndTs(java.time.LocalDateTime.now());
        opVisitRepository.save(visit);

        return PrescriptionResponse.toResponse(prescription, resolveStockChips(prescription));
    }

    /**
     * Live stock-availability chip per line (§8.7) - resolved against the
     * visit's own facility, never blank: IN_STOCK / LOW_STOCK /
     * OUT_OF_STOCK when Pharmacy is active there and a catalogue match
     * exists, PHARMACY_NOT_ACTIVE when the module isn't, or NOT_MATCHED
     * when Pharmacy is active but nothing in the catalogue matches this
     * line's free-text medicine name (module-independence, §5.2/§8.7).
     */
    private List<PrescriptionLineResponse> resolveStockChips(Prescription prescription) {
        boolean pharmacyActive = prescription.getFacility().getActiveModules().contains("PHARMACY");
        Long facilityId = prescription.getFacility().getId();
        Map<Long, Integer> stockByMedicine = pharmacyActive ? aggregateStock(facilityId) : Map.of();

        return prescription.getLines().stream()
                .map(line -> {
                    PrescriptionLineResponse response = PrescriptionLineResponse.toResponse(line);
                    if (!pharmacyActive) {
                        response.setStockStatus("PHARMACY_NOT_ACTIVE");
                        return response;
                    }
                    Integer qty = line.getMatchedPharmacyMedicineId() != null
                            ? stockByMedicine.get(line.getMatchedPharmacyMedicineId())
                            : null;
                    if (qty == null) {
                        response.setStockStatus("NOT_MATCHED");
                    } else {
                        response.setAvailableQty(qty);
                        response.setStockStatus(qty <= 0 ? "OUT_OF_STOCK" : qty < 10 ? "LOW_STOCK" : "IN_STOCK");
                    }
                    return response;
                })
                .toList();
    }

    /** Autocomplete while authoring a line (§8.7) - scoped to the visit's own facility, never the doctor's (they have none). */
    public MedicineLookupResponse lookupMedicine(Long opVisitId, String search) {
        OpVisit visit = ownedVisit(opVisitId);
        boolean pharmacyActive = visit.getFacility().getActiveModules().contains("PHARMACY");
        if (!pharmacyActive || !StringUtils.hasText(search)) {
            return MedicineLookupResponse.builder().pharmacyActive(pharmacyActive).results(List.of()).build();
        }

        Long facilityId = visit.getFacility().getId();
        Map<Long, Integer> stockByMedicine = aggregateStock(facilityId);
        List<Medicine> matches = medicineRepository.searchByNameOrGeneric(facilityId, search);
        List<MedicineStockResponse> results = matches.stream()
                .map(m -> MedicineStockResponse.of(m, stockByMedicine.getOrDefault(m.getId(), 0)))
                .toList();
        return MedicineLookupResponse.builder().pharmacyActive(true).results(results).build();
    }

    private Map<Long, Integer> aggregateStock(Long facilityId) {
        Map<Long, Integer> stockByMedicine = new HashMap<>();
        for (Object[] row : medicineBatchRepository.aggregateActiveStockByMedicine(facilityId)) {
            stockByMedicine.put((Long) row[0], ((Number) row[1]).intValue());
        }
        return stockByMedicine;
    }

    private OpVisit ownedVisit(Long opVisitId) {
        Doctor doctor = currentDoctor();
        OpVisit visit = opVisitRepository.findById(opVisitId)
                .orElseThrow(() -> new ResourceNotFoundException("OP visit not found with id: " + opVisitId));
        if (!visit.getDoctor().getId().equals(doctor.getId())) {
            throw new AccessDeniedException("This visit does not belong to you");
        }
        return visit;
    }

    private Doctor currentDoctor() {
        Long userId = SecurityUtils.currentUserId();
        return doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No Doctor profile for the current user"));
    }
}
