package com.lockdoc.app.service.doctor;

import com.lockdoc.app.config.SecurityUtils;
import com.lockdoc.app.dto.DoctorSearchResult;
import com.lockdoc.app.dto.doctor.DoctorFacilityMappingResponse;
import com.lockdoc.app.dto.doctor.DoctorStatedHoursResponse;
import com.lockdoc.app.dto.doctor.FacilityMappingInviteRequest;
import com.lockdoc.app.dto.doctor.ConsultationHourRequest;
import com.lockdoc.app.dto.doctor.FacilityMappingRequestRequest;
import com.lockdoc.app.dto.doctor.FacilitySearchResult;
import com.lockdoc.app.entity.Doctor;
import com.lockdoc.app.entity.Facility;
import com.lockdoc.app.entity.doctor.DoctorFacilityMapping;
import com.lockdoc.app.entity.doctor.DoctorFacilityMappingHour;
import com.lockdoc.app.entity.op.Appointment;
import com.lockdoc.app.exception.DuplicateResourceException;
import com.lockdoc.app.exception.InvalidDocumentStateException;
import com.lockdoc.app.exception.ResourceNotFoundException;
import com.lockdoc.app.repository.DoctorRepository;
import com.lockdoc.app.repository.FacilityRepository;
import com.lockdoc.app.repository.doctor.DoctorFacilityMappingRepository;
import com.lockdoc.app.repository.op.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * The doctor-facility relationship (Master Spec §8.1), openable from
 * either side: a Hospital/Clinic Admin invites a verified doctor, or a
 * verified doctor requests a facility. Whoever opens it, the other side
 * responds - so every accept/decline path here checks
 * {@code initiatedBy} before acting, which is what stops a doctor
 * approving their own request or an admin their own invite. Either side
 * can end an accepted mapping.
 *
 * Build order step 4's own necessary infrastructure: §8.3's live status
 * is "always scoped to a specific facility", which needs a real,
 * confirmed relationship to scope against - so this exists now even
 * though it wasn't itself named as step 4's deliverable, the same way
 * multi-tenancy (step 1) had to exist before anything could be
 * facility-scoped at all.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class DoctorFacilityMappingService {

    private static final List<String> OPEN_STATUSES =
            List.of(DoctorFacilityMapping.STATUS_REQUESTED, DoctorFacilityMapping.STATUS_ACCEPTED);

    private static final List<String> LIVE_APPOINTMENT_STATUSES =
            List.of(Appointment.STATUS_BOOKED, Appointment.STATUS_ARRIVED, Appointment.STATUS_IN_CONSULTATION);

    private final DoctorFacilityMappingRepository mappingRepository;
    private final DoctorRepository doctorRepository;
    private final FacilityRepository facilityRepository;
    private final AppointmentRepository appointmentRepository;

    /**
     * Lightweight lookup so the add-doctor flow (§17.7 #15, Hospital/Clinic
     * Admin side) has something to search against - Hospital Admin has no
     * access to Super Admin's full doctor-verification list, only enough
     * to find a doctor they already know by name/registration number and
     * invite them. Verified doctors only, matching invite()'s own guard.
     */
    public List<DoctorSearchResult> searchVerifiedDoctors(String search) {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Doctor> page = search != null && !search.isBlank()
                ? doctorRepository.search(search, pageable)
                : doctorRepository.findByVerificationStatus(Doctor.VERIFICATION_VERIFIED, pageable);
        return page.getContent().stream()
                .filter(d -> Doctor.VERIFICATION_VERIFIED.equals(d.getVerificationStatus()))
                .map(d -> new DoctorSearchResult(d.getId(), d.getFullName(), d.getRegistrationNumber(), d.getSpecialties()))
                .toList();
    }

    /** Hospital/Clinic Admin inviting a doctor to their own facility. */
    public DoctorFacilityMappingResponse invite(FacilityMappingInviteRequest request) {
        Long facilityId = SecurityUtils.requireFacilityId();

        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + request.getDoctorId()));
        if (!Doctor.VERIFICATION_VERIFIED.equals(doctor.getVerificationStatus())) {
            throw new InvalidDocumentStateException("Doctor is not yet verified by Super Admin - cannot invite: " + doctor.getFullName());
        }

        Facility facility = facilityRepository.findById(facilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Facility not found with id: " + facilityId));

        boolean alreadyOpen = !mappingRepository
                .findByDoctorIdAndFacilityIdAndStatusIn(doctor.getId(), facilityId, OPEN_STATUSES)
                .isEmpty();
        if (alreadyOpen) {
            throw new DuplicateResourceException(
                    "A request or active mapping already exists between this doctor and facility");
        }

        DoctorFacilityMapping mapping = DoctorFacilityMapping.builder()
                .doctor(doctor)
                .facility(facility)
                .relationshipType(request.getRelationshipType())
                .status(DoctorFacilityMapping.STATUS_REQUESTED)
                .initiatedBy(DoctorFacilityMapping.INITIATED_BY_FACILITY)
                .build();
        return DoctorFacilityMappingResponse.toResponse(mappingRepository.save(mapping));
    }

    /**
     * Hospitals and clinics a doctor can ask to practise at. Verified and
     * active only - requesting a facility Super Admin hasn't verified (or
     * has suspended) would create a relationship the platform can't stand
     * behind - and clinical facility types only, since a pharmacy or lab
     * has no consultations for a doctor to hold.
     */
    public List<FacilitySearchResult> searchRequestableFacilities(String search) {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Facility> page = search != null && !search.isBlank()
                ? facilityRepository.search(search, pageable)
                : facilityRepository.findByVerificationStatus(Facility.VERIFICATION_VERIFIED, pageable);
        return page.getContent().stream()
                .filter(f -> Facility.VERIFICATION_VERIFIED.equals(f.getVerificationStatus()))
                .filter(f -> Boolean.TRUE.equals(f.getActive()))
                .filter(f -> Facility.TYPE_HOSPITAL.equals(f.getType()) || Facility.TYPE_CLINIC.equals(f.getType()))
                .map(f -> new FacilitySearchResult(f.getId(), f.getName(), f.getType(), f.getAddress()))
                .toList();
    }

    /**
     * A verified doctor asking to practise at a facility - the mirror of
     * {@link #invite}. Same verified-only and no-duplicate-open-mapping
     * guards, because the direction the request travels in doesn't change
     * what makes a relationship valid.
     */
    public DoctorFacilityMappingResponse requestFacility(FacilityMappingRequestRequest request) {
        Doctor doctor = currentDoctor();
        if (!Doctor.VERIFICATION_VERIFIED.equals(doctor.getVerificationStatus())) {
            throw new InvalidDocumentStateException(
                    "Your profile is not verified yet - a Super Admin has to verify you before you can request a facility");
        }

        Facility facility = facilityRepository.findById(request.getFacilityId())
                .orElseThrow(() -> new ResourceNotFoundException("Facility not found with id: " + request.getFacilityId()));
        if (!Facility.VERIFICATION_VERIFIED.equals(facility.getVerificationStatus()) || !Boolean.TRUE.equals(facility.getActive())) {
            throw new InvalidDocumentStateException("This facility is not currently accepting doctors: " + facility.getName());
        }

        boolean alreadyOpen = !mappingRepository
                .findByDoctorIdAndFacilityIdAndStatusIn(doctor.getId(), facility.getId(), OPEN_STATUSES)
                .isEmpty();
        if (alreadyOpen) {
            throw new DuplicateResourceException("A request or active mapping already exists between you and this facility");
        }

        DoctorFacilityMapping mapping = DoctorFacilityMapping.builder()
                .doctor(doctor)
                .facility(facility)
                .relationshipType(request.getRelationshipType() != null
                        ? request.getRelationshipType()
                        : DoctorFacilityMapping.RELATIONSHIP_VISITS_ONLY)
                .status(DoctorFacilityMapping.STATUS_REQUESTED)
                .initiatedBy(DoctorFacilityMapping.INITIATED_BY_DOCTOR)
                .build();
        applyConsultationHours(mapping, request.getConsultationHours());
        return DoctorFacilityMappingResponse.toResponse(mappingRepository.save(mapping));
    }

    /**
     * The doctor stating (or restating) when they will be consulting at a
     * facility (§8.2). Separate from the request itself because hours change:
     * a doctor who drops their Saturday clinic says so here, and reception
     * sees it without waiting for an admin to notice.
     *
     * Only the doctor owns this — the facility's own view of the week is its
     * doctor_schedules, which it edits in Masters.
     */
    public DoctorFacilityMappingResponse setConsultationHours(Long mappingId, List<ConsultationHourRequest> hours) {
        Doctor doctor = currentDoctor();
        DoctorFacilityMapping mapping = mappingRepository.findById(mappingId)
                .orElseThrow(() -> new ResourceNotFoundException("Mapping not found with id: " + mappingId));
        if (!mapping.getDoctor().getId().equals(doctor.getId())) {
            throw new AccessDeniedException("This mapping is not yours");
        }
        if (!OPEN_STATUSES.contains(mapping.getStatus())) {
            throw new InvalidDocumentStateException(
                    "This mapping is " + mapping.getStatus().toLowerCase() + " - hours can only be set on a live or pending one");
        }
        applyConsultationHours(mapping, hours);
        return DoctorFacilityMappingResponse.toResponse(mappingRepository.save(mapping));
    }

    /** What every doctor working here says their hours are — the front desk's read of §8.2. */
    @Transactional(readOnly = true)
    public List<DoctorStatedHoursResponse> statedHoursForMyFacility() {
        Long facilityId = SecurityUtils.requireFacilityId();
        return mappingRepository.findByFacilityIdAndStatus(facilityId, DoctorFacilityMapping.STATUS_ACCEPTED).stream()
                .map(DoctorStatedHoursResponse::toResponse)
                .sorted(Comparator.comparing(DoctorStatedHoursResponse::getDoctorName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    /** Replaces the stated hours wholesale — the doctor's latest answer is the whole answer, not an addition to the last one. */
    private void applyConsultationHours(DoctorFacilityMapping mapping, List<ConsultationHourRequest> hours) {
        mapping.getConsultationHours().clear();
        if (hours == null) {
            return;
        }
        for (ConsultationHourRequest h : hours) {
            if (!h.getEndTime().isAfter(h.getStartTime())) {
                throw new InvalidDocumentStateException(
                        "A consulting window has to end after it starts - check " + h.getWeekday().toLowerCase());
            }
            mapping.getConsultationHours().add(DoctorFacilityMappingHour.builder()
                    .mapping(mapping)
                    .weekday(h.getWeekday().toUpperCase())
                    .startTime(h.getStartTime())
                    .endTime(h.getEndTime())
                    .build());
        }
    }

    /** Doctor accepting a REQUESTED invite addressed to them - never one they raised themselves. */
    public DoctorFacilityMappingResponse accept(Long id) {
        DoctorFacilityMapping mapping = findOwnedByCurrentDoctor(id);
        requireStatus(mapping, DoctorFacilityMapping.STATUS_REQUESTED, "accepted");
        requireInitiator(mapping, DoctorFacilityMapping.INITIATED_BY_FACILITY,
                "You raised this request - the facility has to respond to it, you can't accept it yourself");
        mapping.setStatus(DoctorFacilityMapping.STATUS_ACCEPTED);
        mapping.setRespondedAt(LocalDateTime.now());
        return DoctorFacilityMappingResponse.toResponse(mappingRepository.save(mapping));
    }

    /** Doctor declining an invite, or withdrawing a request they raised - both land on DECLINED. */
    public DoctorFacilityMappingResponse decline(Long id) {
        DoctorFacilityMapping mapping = findOwnedByCurrentDoctor(id);
        requireStatus(mapping, DoctorFacilityMapping.STATUS_REQUESTED, "declined");
        mapping.setStatus(DoctorFacilityMapping.STATUS_DECLINED);
        mapping.setRespondedAt(LocalDateTime.now());
        return DoctorFacilityMappingResponse.toResponse(mappingRepository.save(mapping));
    }

    /** Hospital/Clinic Admin approving a doctor-raised request at their own facility. */
    public DoctorFacilityMappingResponse approveRequest(Long id) {
        DoctorFacilityMapping mapping = findOwnedByCurrentFacility(id);
        requireStatus(mapping, DoctorFacilityMapping.STATUS_REQUESTED, "accepted");
        requireInitiator(mapping, DoctorFacilityMapping.INITIATED_BY_DOCTOR,
                "Your facility sent this invite - the doctor has to respond to it, you can't accept it on their behalf");
        mapping.setStatus(DoctorFacilityMapping.STATUS_ACCEPTED);
        mapping.setRespondedAt(LocalDateTime.now());
        return DoctorFacilityMappingResponse.toResponse(mappingRepository.save(mapping));
    }

    /** Hospital/Clinic Admin declining a doctor-raised request, or withdrawing an invite they sent. */
    public DoctorFacilityMappingResponse declineRequest(Long id) {
        DoctorFacilityMapping mapping = findOwnedByCurrentFacility(id);
        requireStatus(mapping, DoctorFacilityMapping.STATUS_REQUESTED, "declined");
        mapping.setStatus(DoctorFacilityMapping.STATUS_DECLINED);
        mapping.setRespondedAt(LocalDateTime.now());
        return DoctorFacilityMappingResponse.toResponse(mappingRepository.save(mapping));
    }

    /**
     * Either side can end an ACCEPTED mapping (§8.1). §8.1 also requires
     * ending a mapping with future booked appointments to surface them
     * and force a reschedule/cancel decision "in the same action" - this
     * was a deliberate no-op while Appointment didn't exist yet (build
     * order step 4's own note); Appointment has existed since step 5 and
     * the check was never revisited, so ending a mapping was silently
     * orphaning future bookings this whole time. Fixed here as a safety
     * net: blocks the end with a clear error rather than the fuller
     * inline reschedule/cancel UX §8.1 describes, which is real
     * additional screen work belonging to a future pass, not silently
     * left broken in the meantime.
     */
    public DoctorFacilityMappingResponse end(Long id) {
        var principal = SecurityUtils.currentPrincipal();
        DoctorFacilityMapping mapping = mappingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mapping not found with id: " + id));

        if (principal.isDoctor()) {
            Doctor doctor = currentDoctor();
            if (!mapping.getDoctor().getId().equals(doctor.getId())) {
                throw new AccessDeniedException("This mapping does not belong to you");
            }
        } else {
            Long facilityId = SecurityUtils.requireFacilityId();
            if (!mapping.getFacility().getId().equals(facilityId)) {
                throw new AccessDeniedException("This mapping does not belong to your facility");
            }
        }

        requireStatus(mapping, DoctorFacilityMapping.STATUS_ACCEPTED, "ended");

        boolean hasFutureAppointments = appointmentRepository.existsByDoctorIdAndFacilityIdAndAppointmentTsAfterAndStatusIn(
                mapping.getDoctor().getId(), mapping.getFacility().getId(), LocalDateTime.now(), LIVE_APPOINTMENT_STATUSES);
        if (hasFutureAppointments) {
            throw new InvalidDocumentStateException(
                    "This doctor has future booked appointments at this facility - reschedule or cancel them before ending the mapping");
        }

        mapping.setStatus(DoctorFacilityMapping.STATUS_ENDED);
        mapping.setEndedAt(LocalDateTime.now());
        return DoctorFacilityMappingResponse.toResponse(mappingRepository.save(mapping));
    }

    public List<DoctorFacilityMappingResponse> myMappings() {
        Doctor doctor = currentDoctor();
        return mappingRepository.findByDoctorIdOrderByRequestedAtDesc(doctor.getId()).stream()
                .map(DoctorFacilityMappingResponse::toResponse)
                .toList();
    }

    public List<DoctorFacilityMappingResponse> facilityMappings() {
        Long facilityId = SecurityUtils.requireFacilityId();
        return mappingRepository.findByFacilityIdOrderByRequestedAtDesc(facilityId).stream()
                .map(DoctorFacilityMappingResponse::toResponse)
                .toList();
    }

    private DoctorFacilityMapping findOwnedByCurrentDoctor(Long mappingId) {
        Doctor doctor = currentDoctor();
        return mappingRepository.findByIdAndDoctorId(mappingId, doctor.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Mapping not found for this doctor with id: " + mappingId));
    }

    private DoctorFacilityMapping findOwnedByCurrentFacility(Long mappingId) {
        Long facilityId = SecurityUtils.requireFacilityId();
        return mappingRepository.findByIdAndFacilityId(mappingId, facilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Mapping not found for this facility with id: " + mappingId));
    }

    private void requireStatus(DoctorFacilityMapping mapping, String required, String action) {
        if (!required.equals(mapping.getStatus())) {
            throw new InvalidDocumentStateException(
                    "Mapping can only be " + action + " from " + required + " status, current status: " + mapping.getStatus());
        }
    }

    /** Two-sided confirmation only means anything if the side that opened a request can't also close it. */
    private void requireInitiator(DoctorFacilityMapping mapping, String required, String message) {
        if (!required.equals(mapping.getInitiatedBy())) {
            throw new InvalidDocumentStateException(message);
        }
    }

    private Doctor currentDoctor() {
        Long userId = SecurityUtils.currentUserId();
        return doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No Doctor profile for the current user"));
    }
}
