package com.lockdoc.outpatient.service;

import com.lockdoc.common.config.SecurityUtils;
import com.lockdoc.outpatient.dto.*;
import com.lockdoc.common.entity.Facility;
import com.lockdoc.outpatient.entity.Appointment;
import com.lockdoc.outpatient.entity.AppointmentWaitlist;
import com.lockdoc.outpatient.entity.DoctorSchedule;
import com.lockdoc.outpatient.entity.Patient;
import com.lockdoc.common.exception.InvalidDocumentStateException;
import com.lockdoc.common.exception.ResourceNotFoundException;
import com.lockdoc.common.repository.FacilityRepository;
import com.lockdoc.outpatient.repository.AppointmentRepository;
import com.lockdoc.outpatient.repository.AppointmentWaitlistRepository;
import com.lockdoc.outpatient.repository.DoctorScheduleRepository;
import com.lockdoc.outpatient.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Booking/reschedule + waitlist (Master Spec §7.3). Capacity + overbook
 * enforcement (§6 invariant 5) locks every live appointment row for the
 * target schedule+day via {@code PESSIMISTIC_WRITE} before counting, so
 * two concurrent bookings for the last slot can't both read the same
 * under-capacity count and both succeed - exactly the "confirmed
 * bookings never exceed capacity + overbook allowance" guarantee the
 * invariant requires.
 *
 * Cancellation is NOT here - per §4.1, a Receptionist's cancellation
 * request is always Pending Approval, so it goes through
 * {@link CancellationService} instead of a direct method on this
 * service.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentWaitlistRepository waitlistRepository;
    private final DoctorScheduleRepository scheduleRepository;
    private final PatientRepository patientRepository;
    private final FacilityRepository facilityRepository;

    public AppointmentResponse book(AppointmentRequest request) {
        Long facilityId = SecurityUtils.requireFacilityId();

        DoctorSchedule schedule = scheduleRepository.findByIdAndFacilityId(request.getDoctorScheduleId(), facilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor schedule not found with id: " + request.getDoctorScheduleId()));
        Patient patient = patientRepository.findByIdAndFacilityId(request.getPatientId(), facilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + request.getPatientId()));

        assertCapacityAvailable(schedule, request.getAppointmentTs());

        Facility facility = facilityRepository.getReferenceById(facilityId);
        Appointment appointment = Appointment.builder()
                .facility(facility)
                .doctor(schedule.getDoctor())
                .doctorSchedule(schedule)
                .patient(patient)
                .appointmentTs(request.getAppointmentTs())
                .status(Appointment.STATUS_BOOKED)
                .purpose(Appointment.PURPOSE_PROCEDURE.equalsIgnoreCase(request.getPurpose())
                        ? Appointment.PURPOSE_PROCEDURE
                        : Appointment.PURPOSE_CONSULTATION)
                .channel(StringUtils.hasText(request.getChannel()) ? request.getChannel() : Appointment.CHANNEL_FRONT_DESK)
                .build();
        return AppointmentResponse.toResponse(appointmentRepository.save(appointment));
    }

    public AppointmentResponse reschedule(Long id, AppointmentRescheduleRequest request) {
        return reschedule(id, request, SecurityUtils.requireFacilityId());
    }

    /**
     * Facility-explicit overload so a caller that isn't itself
     * facility-scoped (a Doctor, whose token carries no facility_id
     * since they span several - see SecurityUtils) can still reschedule
     * an appointment at a facility it has already resolved and
     * authorized by other means. Used by DoctorAvailabilityService when
     * a schedule-exception resolution moves an affected appointment
     * instead of cancelling it (§8.2). The facility-scoped controller
     * path above still goes through {@link SecurityUtils#requireFacilityId()}.
     */
    public AppointmentResponse reschedule(Long id, AppointmentRescheduleRequest request, Long facilityId) {
        Appointment appointment = appointmentRepository.findByIdAndFacilityId(id, facilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));
        if (!Appointment.STATUS_BOOKED.equals(appointment.getStatus())) {
            throw new InvalidDocumentStateException("Only a BOOKED appointment can be rescheduled, current status: " + appointment.getStatus());
        }

        DoctorSchedule targetSchedule = request.getNewDoctorScheduleId() != null
                ? scheduleRepository.findByIdAndFacilityId(request.getNewDoctorScheduleId(), facilityId)
                        .orElseThrow(() -> new ResourceNotFoundException("Doctor schedule not found with id: " + request.getNewDoctorScheduleId()))
                : appointment.getDoctorSchedule();

        assertCapacityAvailable(targetSchedule, request.getNewAppointmentTs());

        appointment.setDoctorSchedule(targetSchedule);
        appointment.setDoctor(targetSchedule.getDoctor());
        appointment.setAppointmentTs(request.getNewAppointmentTs());
        return AppointmentResponse.toResponse(appointmentRepository.save(appointment));
    }

    /**
     * A direct cancel, bypassing {@link CancellationService}'s
     * pending-approval queue - deliberately, not an oversight. That
     * queue is for a discretionary front-desk cancellation someone else
     * may need to approve; this one is the forced, non-discretionary
     * consequence of a doctor blocking their own already-authorized
     * schedule (§8.2 - "force an explicit reschedule/cancel decision
     * before the block commits"), decided by the same person the
     * appointment's own doctor is, with no one else to ask. Used only by
     * DoctorAvailabilityService's schedule-exception resolution.
     */
    public AppointmentResponse directCancel(Long id, Long facilityId, String reason) {
        Appointment appointment = appointmentRepository.findByIdAndFacilityId(id, facilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));
        if (!Appointment.STATUS_BOOKED.equals(appointment.getStatus()) && !Appointment.STATUS_ARRIVED.equals(appointment.getStatus())) {
            throw new InvalidDocumentStateException("Only a BOOKED or ARRIVED appointment can be cancelled this way, current status: " + appointment.getStatus());
        }
        appointment.setStatus(Appointment.STATUS_CANCELLED);
        appointment.setCancelReason(reason);
        return AppointmentResponse.toResponse(appointmentRepository.save(appointment));
    }

    public List<AppointmentResponse> listForFacilityToday() {
        Long facilityId = SecurityUtils.requireFacilityId();
        LocalDate today = LocalDate.now();
        return appointmentRepository
                .findByFacilityIdAndAppointmentTsBetweenOrderByAppointmentTsAsc(facilityId, today.atStartOfDay(), today.plusDays(1).atStartOfDay())
                .stream().map(AppointmentResponse::toResponse).toList();
    }

    public WaitlistResponse joinWaitlist(WaitlistJoinRequest request) {
        Long facilityId = SecurityUtils.requireFacilityId();
        DoctorSchedule schedule = scheduleRepository.findByIdAndFacilityId(request.getDoctorScheduleId(), facilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor schedule not found with id: " + request.getDoctorScheduleId()));
        Patient patient = patientRepository.findByIdAndFacilityId(request.getPatientId(), facilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + request.getPatientId()));
        Facility facility = facilityRepository.getReferenceById(facilityId);

        AppointmentWaitlist entry = AppointmentWaitlist.builder()
                .facility(facility)
                .doctorSchedule(schedule)
                .sessionDate(request.getSessionDate())
                .patient(patient)
                .build();
        return WaitlistResponse.toResponse(waitlistRepository.save(entry));
    }

    public List<WaitlistResponse> listWaitlist(Long doctorScheduleId, LocalDate sessionDate) {
        return waitlistRepository.findByDoctorScheduleIdAndSessionDateAndPromotedAtIsNullOrderByJoinedAtAsc(doctorScheduleId, sessionDate)
                .stream().map(WaitlistResponse::toResponse).toList();
    }

    /** Front desk manually promotes when a slot frees (§7.3 - no automatic notification in this build). */
    public AppointmentResponse promoteFromWaitlist(Long waitlistId) {
        Long facilityId = SecurityUtils.requireFacilityId();
        AppointmentWaitlist entry = waitlistRepository.findByIdAndFacilityId(waitlistId, facilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Waitlist entry not found with id: " + waitlistId));
        if (entry.getPromotedAt() != null) {
            throw new InvalidDocumentStateException("This waitlist entry has already been promoted");
        }

        DoctorSchedule schedule = entry.getDoctorSchedule();
        LocalDateTime slotTs = entry.getSessionDate().atTime(schedule.getStartTime());
        assertCapacityAvailable(schedule, slotTs);

        Facility facility = facilityRepository.getReferenceById(facilityId);
        Appointment appointment = Appointment.builder()
                .facility(facility)
                .doctor(schedule.getDoctor())
                .doctorSchedule(schedule)
                .patient(entry.getPatient())
                .appointmentTs(slotTs)
                .status(Appointment.STATUS_BOOKED)
                .purpose(Appointment.PURPOSE_CONSULTATION)
                .channel(Appointment.CHANNEL_FRONT_DESK)
                .build();
        appointment = appointmentRepository.save(appointment);

        entry.setPromotedAt(LocalDateTime.now());
        entry.setPromotedAppointmentId(appointment.getId());
        waitlistRepository.save(entry);

        return AppointmentResponse.toResponse(appointment);
    }

    private void assertCapacityAvailable(DoctorSchedule schedule, LocalDateTime appointmentTs) {
        LocalDateTime dayStart = appointmentTs.toLocalDate().atStartOfDay();
        LocalDateTime dayEnd = dayStart.plusDays(1);
        List<Appointment> live = appointmentRepository.findLiveForUpdate(schedule.getId(), dayStart, dayEnd);
        int allowed = schedule.getCapacity() + schedule.getOverbookAllowance();
        if (live.size() >= allowed) {
            throw new InvalidDocumentStateException(
                    "This session is full (" + live.size() + "/" + allowed + ") - join the waitlist instead");
        }
    }
}
