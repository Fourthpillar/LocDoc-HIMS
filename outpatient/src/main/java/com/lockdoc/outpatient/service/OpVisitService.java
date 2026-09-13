package com.lockdoc.outpatient.service;

import com.lockdoc.common.config.SecurityUtils;
import com.lockdoc.outpatient.dto.ConsultationRatingRequest;
import com.lockdoc.outpatient.dto.ConsultationRatingResponse;
import com.lockdoc.outpatient.dto.OpVisitRequest;
import com.lockdoc.outpatient.dto.OpVisitResponse;
import com.lockdoc.outpatient.dto.VisitChargePreviewResponse;
import com.lockdoc.common.entity.Doctor;
import com.lockdoc.common.entity.Facility;
import com.lockdoc.common.entity.ConsultationRate;
import com.lockdoc.outpatient.entity.FreeReviewLink;
import com.lockdoc.common.entity.FreeReviewPolicy;
import com.lockdoc.outpatient.entity.Appointment;
import com.lockdoc.outpatient.entity.Bill;
import com.lockdoc.outpatient.entity.ConsultationRating;
import com.lockdoc.outpatient.entity.OpVisit;
import com.lockdoc.outpatient.entity.PatientRegistration;
import com.lockdoc.outpatient.entity.RegistrationFeeConfig;
import com.lockdoc.outpatient.entity.Patient;
import com.lockdoc.common.exception.InvalidDocumentStateException;
import com.lockdoc.common.exception.ResourceNotFoundException;
import com.lockdoc.common.repository.DoctorRepository;
import com.lockdoc.common.repository.FacilityRepository;
import com.lockdoc.common.repository.ConsultationRateRepository;
import com.lockdoc.outpatient.repository.FreeReviewLinkRepository;
import com.lockdoc.common.repository.FreeReviewPolicyRepository;
import com.lockdoc.outpatient.repository.AppointmentRepository;
import com.lockdoc.outpatient.repository.BillRepository;
import com.lockdoc.outpatient.repository.ConsultationRatingRepository;
import com.lockdoc.outpatient.repository.OpVisitRepository;
import com.lockdoc.outpatient.repository.PatientRegistrationRepository;
import com.lockdoc.outpatient.repository.RegistrationFeeConfigRepository;
import com.lockdoc.outpatient.repository.PatientRepository;
import com.lockdoc.common.service.DocumentNumberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Front-desk visit capture (Master Spec §7.4) - deliberately non-clinical
 * (§7.1); the clinical narrative is the Doctor Module's own screen,
 * build order step 6.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class OpVisitService {

    private static final String DOC_TYPE = "OP_VISIT";
    private static final String PREFIX = "OP";

    private final OpVisitRepository visitRepository;
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final FacilityRepository facilityRepository;
    private final ConsultationRateRepository consultationRateRepository;
    private final BillingService billingService;
    private final DocumentNumberService documentNumberService;
    private final FreeReviewPolicyRepository freeReviewPolicyRepository;
    private final FreeReviewLinkRepository freeReviewLinkRepository;
    private final BillRepository billRepository;
    private final com.lockdoc.outpatient.repository.ReferralDoctorRepository referralDoctorRepository;
    private final com.lockdoc.outpatient.repository.ProRepository proRepository;
    private final ConsultationRatingRepository ratingRepository;
    private final PatientRegistrationRepository registrationRepository;
    private final RegistrationFeeConfigRepository registrationFeeConfigRepository;

    public OpVisitResponse arrive(OpVisitRequest request) {
        Long facilityId = SecurityUtils.requireFacilityId();

        Patient patient = patientRepository.findByIdAndFacilityId(request.getPatientId(), facilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + request.getPatientId()));
        requireValidRegistration(patient, facilityId);
        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + request.getDoctorId()));
        Facility facility = facilityRepository.getReferenceById(facilityId);

        com.lockdoc.outpatient.entity.ReferralDoctor referralDoctor = null;
        if (request.getReferralDoctorId() != null) {
            referralDoctor = referralDoctorRepository.findByIdAndFacilityId(request.getReferralDoctorId(), facilityId)
                    .orElseThrow(() -> new ResourceNotFoundException("Referral doctor not found with id: " + request.getReferralDoctorId()));
        }
        com.lockdoc.outpatient.entity.Pro pro = null;
        if (request.getProId() != null) {
            pro = proRepository.findByIdAndFacilityId(request.getProId(), facilityId)
                    .orElseThrow(() -> new ResourceNotFoundException("PRO not found with id: " + request.getProId()));
        }

        if (request.getAppointmentId() != null) {
            Appointment appointment = appointmentRepository.findByIdAndFacilityId(request.getAppointmentId(), facilityId)
                    .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + request.getAppointmentId()));
            appointment.setStatus(Appointment.STATUS_ARRIVED);
            appointmentRepository.save(appointment);
        }

        OpVisit visit = OpVisit.builder()
                .opNo(documentNumberService.next(facilityId, DOC_TYPE, PREFIX))
                .facility(facility)
                .patient(patient)
                .appointmentId(request.getAppointmentId())
                .doctor(doctor)
                .orgType(StringUtils.hasText(request.getOrgType()) ? request.getOrgType() : "DIRECT")
                .visitType(StringUtils.hasText(request.getVisitType()) ? request.getVisitType() : OpVisit.VISIT_NEW)
                .weightKg(request.getWeightKg())
                .heightCm(request.getHeightCm())
                .temperatureF(request.getTemperatureF())
                .bp(request.getBp())
                .attendantName(request.getAttendantName())
                .attendantMobile(request.getAttendantMobile())
                .attendantRelation(request.getAttendantRelation())
                .status(OpVisit.STATUS_ARRIVED)
                .mlcFlag(request.getMlcFlag() != null && request.getMlcFlag())
                .mlcPoliceStation(request.getMlcPoliceStation())
                .mlcNumber(request.getMlcNumber())
                .referralDoctor(referralDoctor)
                .pro(pro)
                .build();
        return OpVisitResponse.toResponse(visitRepository.save(visit));
    }

    /**
     * A visit needs a registration that is still good (Master Spec §7.4,
     * Coverage F1). Arrival is where that is enforced rather than booking,
     * because the fee is only ever collected in person — a patient who
     * booked by phone (or, later, from the LocDoc app) has had no chance to
     * pay for one, so the desk registers them as they walk in.
     *
     * Two answers count as good, and which one applies is the facility's
     * own decision (§7.2): a registration whose expiry has not passed, and
     * one with no expiry at all, taken while the facility charged once for
     * life.
     *
     * Registering is not a payment — it raises a bill that sits as due — so
     * blocking here costs the desk one click, not a patient standing at the
     * counter without their wallet.
     */
    private void requireValidRegistration(Patient patient, Long facilityId) {
        PatientRegistration latest = registrationRepository
                .findByPatientIdAndFacilityIdOrderByRegisteredAtDesc(patient.getId(), facilityId)
                .stream().findFirst().orElse(null);

        if (latest == null) {
            throw new InvalidDocumentStateException(
                    patient.getFullName() + " is not registered at this facility - register them before checking them in");
        }
        if (latest.getExpiryDate() != null && latest.getExpiryDate().isBefore(LocalDate.now())) {
            throw new InvalidDocumentStateException(
                    patient.getFullName() + "'s registration expired on " + latest.getExpiryDate() + " - re-register them before checking them in");
        }
    }

    /**
     * What this patient would owe if they saw this doctor (Master Spec
     * §7.2/§7.4/§8.6), answered at booking time instead of at the counter.
     *
     * Everything here is a read: nothing is registered, nothing is billed, and
     * asking twice changes nothing. It exists because the three facts that
     * decide the money are all invisible to whoever is taking the booking —
     * whether the registration still holds, whether the doctor has an approved
     * rate, and whether a free review covers the visit.
     *
     * All three are resolved <em>as of the appointment</em>, not as of now, and
     * that difference is the whole point of passing {@code when}: a registration
     * valid today can lapse before a slot three weeks out, a night rate applies
     * to an 8pm booking made at noon, and a free-review window that covers the
     * patient today may have closed by the date they actually come. Quoting
     * "now" for a future slot quietly told the desk the wrong number.
     *
     * What it cannot promise is a rate nobody has approved yet. The figure is the
     * rate that would apply if the appointment happened under today's approved
     * rates; if an admin approves a new one before the patient arrives, the new
     * one bills. The note says so whenever the slot isn't today.
     */
    @Transactional(readOnly = true)
    public VisitChargePreviewResponse previewCharges(Long patientId, Long doctorId, LocalDateTime when) {
        Long facilityId = SecurityUtils.requireFacilityId();
        LocalDateTime at = when == null ? LocalDateTime.now() : when;
        LocalDate onDate = at.toLocalDate();
        boolean future = onDate.isAfter(LocalDate.now());

        Patient patient = patientRepository.findByIdAndFacilityId(patientId, facilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + patientId));

        PatientRegistration latest = registrationRepository
                .findByPatientIdAndFacilityIdOrderByRegisteredAtDesc(patientId, facilityId)
                .stream().findFirst().orElse(null);
        RegistrationFeeConfig feeConfig = registrationFeeConfigRepository.findByFacilityId(facilityId).orElse(null);

        // Valid on the day of the appointment — not valid today, which is a different question
        // for any slot far enough out that the registration lapses in between.
        boolean registered = latest != null && (latest.getExpiryDate() == null || !latest.getExpiryDate().isBefore(onDate));
        BigDecimal registrationFee = BigDecimal.ZERO;
        String registrationNote;
        if (registered) {
            registrationNote = latest.getExpiryDate() == null
                    ? "Registered here, no expiry - nothing to pay"
                    : "Registered until " + latest.getExpiryDate() + " - nothing to pay";
        } else if (feeConfig == null) {
            registrationNote = "This facility has not set its registration fee yet";
        } else if (latest == null) {
            registrationFee = feeConfig.getFirstFee();
            registrationNote = "New patient here - registration is charged on arrival";
        } else {
            registrationFee = feeConfig.getReRegistrationFee();
            registrationNote = latest.getExpiryDate().isBefore(LocalDate.now())
                    ? "Registration expired on " + latest.getExpiryDate() + " - re-registration is charged on arrival"
                    : "Registration lapses on " + latest.getExpiryDate() + ", before this date - re-registration is charged on arrival";
        }

        BigDecimal consultationFee = BigDecimal.ZERO;
        boolean free = false;
        String consultationNote;

        Optional<OpVisit> anchor = findFreeReviewAnchor(patientId, doctorId, null, facilityId, at);
        if (anchor.isPresent()) {
            free = true;
            consultationNote = "Free review against the visit on " + anchor.get().getArrivedTs().toLocalDate() + " - no consultation fee";
        } else {
            String dayNight = at.toLocalTime().isBefore(LocalTime.of(20, 0)) && !at.toLocalTime().isBefore(LocalTime.of(6, 0))
                    ? ConsultationRate.DAY : ConsultationRate.NIGHT;
            ConsultationRate rate = consultationRateRepository
                    .findByDoctorIdAndFacilityIdOrderByCreatedDateDesc(doctorId, facilityId)
                    .stream()
                    .filter(r -> ConsultationRate.STATUS_APPROVED.equals(r.getStatus()))
                    .filter(r -> ConsultationRate.ORG_TYPE_DIRECT.equals(r.getOrgType()))
                    .filter(r -> r.getDayNightIndicator().equals(dayNight))
                    // A rate with an effective window only counts if the appointment falls inside it.
                    .filter(r -> r.getEffectiveFrom() == null || !r.getEffectiveFrom().isAfter(onDate))
                    .filter(r -> r.getEffectiveTo() == null || !r.getEffectiveTo().isBefore(onDate))
                    .max(Comparator.comparing(ConsultationRate::getApprovedAt))
                    .orElse(null);
            if (rate == null) {
                consultationNote = "This doctor has no approved fee for " + dayNight.toLowerCase() + " visits on that date yet - arrival will not be billable";
            } else {
                consultationFee = rate.getTotalAmount();
                consultationNote = "Doctor's approved " + dayNight.toLowerCase() + " fee"
                        + (future ? ", at today's rates - what bills is whatever is approved on the day" : "");
            }
        }

        return VisitChargePreviewResponse.builder()
                .registrationRequired(!registered)
                .registrationFee(registrationFee)
                .registrationNote(registrationNote)
                .consultationFee(consultationFee)
                .consultationFree(free)
                .consultationNote(consultationNote)
                .total(registrationFee.add(consultationFee))
                .build();
    }

    public OpVisitResponse get(Long id) {
        return OpVisitResponse.toResponse(findEntity(id));
    }

    public OpVisitResponse startConsultation(Long id) {
        OpVisit visit = findEntity(id);
        visit.setStatus(OpVisit.STATUS_IN_CONSULTATION);
        visit.setConsultStartTs(LocalDateTime.now());
        return OpVisitResponse.toResponse(visitRepository.save(visit));
    }

    public OpVisitResponse complete(Long id) {
        OpVisit visit = findEntity(id);
        visit.setStatus(OpVisit.STATUS_COMPLETED);
        visit.setConsultEndTs(LocalDateTime.now());
        return OpVisitResponse.toResponse(visitRepository.save(visit));
    }

    /**
     * Bills the consultation against the doctor's latest APPROVED
     * consultation rate for this facility+org-type+day-night window
     * (Master Spec §8.6). Throws if none is approved yet - the "newly-
     * mapped doctor, no approved rate" edge case §8.6 calls out: still
     * schedulable and visitable, just not billable as a paid consultation
     * until Hospital/Clinic Admin approves a rate.
     */
    public com.lockdoc.outpatient.dto.BillResponse billConsultation(Long visitId) {
        Long facilityId = SecurityUtils.requireFacilityId();
        Long userId = SecurityUtils.currentUserId();
        OpVisit visit = findEntity(visitId);
        Facility facility = facilityRepository.getReferenceById(facilityId);

        Optional<OpVisit> freeReviewAnchor = findFreeReviewAnchor(visit, facilityId);
        if (freeReviewAnchor.isPresent()) {
            Bill freeBill = billingService.createBill(facility, visit.getPatient(), Bill.ENCOUNTER_CONSULTATION, visit.getId(), BigDecimal.ZERO, userId);
            freeReviewLinkRepository.save(FreeReviewLink.builder()
                    .patient(visit.getPatient())
                    .doctor(visit.getDoctor())
                    .facility(facility)
                    .opVisit(visit)
                    .originalOpVisit(freeReviewAnchor.get())
                    .build());
            return com.lockdoc.outpatient.dto.BillResponse.toResponse(freeBill);
        }

        String dayNight = LocalTime.now().isBefore(LocalTime.of(20, 0)) && !LocalTime.now().isBefore(LocalTime.of(6, 0))
                ? ConsultationRate.DAY : ConsultationRate.NIGHT;

        ConsultationRate rate = consultationRateRepository
                .findByDoctorIdAndFacilityIdOrderByCreatedDateDesc(visit.getDoctor().getId(), facilityId)
                .stream()
                .filter(r -> ConsultationRate.STATUS_APPROVED.equals(r.getStatus()))
                .filter(r -> r.getOrgType().equals(visit.getOrgType()))
                .filter(r -> r.getDayNightIndicator().equals(dayNight))
                .max(Comparator.comparing(ConsultationRate::getApprovedAt))
                .orElseThrow(() -> new InvalidDocumentStateException(
                        "This doctor has no approved consultation fee yet for " + visit.getOrgType() + "/" + dayNight
                                + " at this facility - a Hospital/Clinic Admin must approve one before billing a paid consultation"));

        Bill bill = billingService.createBill(facility, visit.getPatient(), Bill.ENCOUNTER_CONSULTATION, visit.getId(), rate.getTotalAmount(), userId);
        return com.lockdoc.outpatient.dto.BillResponse.toResponse(bill);
    }

    /**
     * Free-review eligibility (§7.4/§7.2) - "max days and max visits, both
     * must hold". Walks this patient-doctor pair's visit history for the
     * most recent visit that was ever billed as a real *paid* consultation
     * (gross &gt; 0) at this facility; if the doctor's policy says this
     * visit is still within maxDays of that anchor, and fewer than
     * maxVisits free reviews have already been counted against it,
     * returns that anchor visit - the caller bills ₹0 and links back to it.
     */
    private Optional<OpVisit> findFreeReviewAnchor(OpVisit visit, Long facilityId) {
        return findFreeReviewAnchor(visit.getPatient().getId(), visit.getDoctor().getId(), visit.getId(), facilityId, LocalDateTime.now());
    }

    /**
     * The same question asked before the visit exists — "if they came at {@code at},
     * would it be free?" (see {@link #previewCharges}). The instant matters: a window
     * that is open today can be shut by the date the patient actually has a slot.
     */
    private Optional<OpVisit> findFreeReviewAnchor(Long patientId, Long doctorId, Long excludeVisitId, Long facilityId, LocalDateTime at) {
        FreeReviewPolicy policy = freeReviewPolicyRepository
                .findByDoctorIdAndFacilityId(doctorId, facilityId)
                .orElse(null);
        if (policy == null) {
            return Optional.empty();
        }

        List<OpVisit> history = visitRepository.findByPatientIdAndDoctorIdOrderByArrivedTsDesc(patientId, doctorId);
        for (OpVisit candidate : history) {
            if (excludeVisitId != null && candidate.getId().equals(excludeVisitId)) continue;
            if (!candidate.getFacility().getId().equals(facilityId)) continue;

            Bill candidateBill = billRepository.findByEncounterTypeAndEncounterId(Bill.ENCOUNTER_CONSULTATION, candidate.getId()).orElse(null);
            if (candidateBill == null || candidateBill.getGross().compareTo(BigDecimal.ZERO) <= 0) {
                continue; // not a paid consultation - either unbilled or itself a free review; keep looking further back
            }

            boolean withinDays = Duration.between(candidate.getArrivedTs(), at).toDays() <= policy.getMaxDays();
            if (!withinDays) {
                return Optional.empty(); // the most recent paid consultation is already outside the window - no anchor
            }

            long alreadyUsed = freeReviewLinkRepository.countByOriginalOpVisitId(candidate.getId());
            return alreadyUsed < policy.getMaxVisits() ? Optional.of(candidate) : Optional.empty();
        }
        return Optional.empty();
    }

    /**
     * Front-desk patient-satisfaction capture (Master Spec §17.7 #18's own
     * gap) - one rating per visit, only once the consultation is actually
     * done (rating a visit still in progress isn't a real review). Saved
     * in place on a second call, e.g. the desk correcting a mis-tap.
     */
    public ConsultationRatingResponse rate(Long opVisitId, ConsultationRatingRequest request) {
        OpVisit visit = findEntity(opVisitId);
        if (!OpVisit.STATUS_COMPLETED.equals(visit.getStatus())) {
            throw new InvalidDocumentStateException("This visit isn't completed yet - a rating can only be captured once the consultation is done");
        }

        ConsultationRating rating = ratingRepository.findByOpVisitId(opVisitId)
                .orElseGet(() -> ConsultationRating.builder()
                        .opVisit(visit)
                        .doctor(visit.getDoctor())
                        .facility(visit.getFacility())
                        .build());
        rating.setRating(request.getRating());
        rating.setComment(StringUtils.hasText(request.getComment()) ? request.getComment().trim() : null);
        rating.setRatedByUserId(SecurityUtils.currentUserId());
        return ConsultationRatingResponse.toResponse(ratingRepository.save(rating));
    }

    /** Null when this visit hasn't been rated yet - a real, un-rated state, not an error. */
    public ConsultationRatingResponse getRating(Long opVisitId) {
        findEntity(opVisitId); // facility-scope check, response discarded
        return ratingRepository.findByOpVisitId(opVisitId).map(ConsultationRatingResponse::toResponse).orElse(null);
    }

    /** Front-desk day list feed (Master Spec §7.4) - today's visits at this facility. */
    public List<OpVisitResponse> listForFacilityToday() {
        Long facilityId = SecurityUtils.requireFacilityId();
        LocalDate today = LocalDate.now();
        return visitRepository
                .findByFacilityIdAndArrivedTsBetweenOrderByArrivedTsAsc(facilityId, today.atStartOfDay(), today.plusDays(1).atStartOfDay())
                .stream().map(OpVisitResponse::toResponse).toList();
    }

    /** One patient's visits at this facility, newest first - what the Patient Record screen reads (§17.7 #3). */
    public List<OpVisitResponse> listForPatient(Long patientId) {
        Long facilityId = SecurityUtils.requireFacilityId();
        return visitRepository.findByPatientIdAndFacilityIdOrderByArrivedTsDesc(patientId, facilityId)
                .stream().map(OpVisitResponse::toResponse).toList();
    }

    private OpVisit findEntity(Long id) {
        Long facilityId = SecurityUtils.requireFacilityId();
        return visitRepository.findByIdAndFacilityId(id, facilityId)
                .orElseThrow(() -> new ResourceNotFoundException("OP visit not found with id: " + id));
    }
}
