package com.lockdoc.app.service.doctor;

import com.lockdoc.app.config.SecurityUtils;
import com.lockdoc.app.dto.doctor.*;
import com.lockdoc.app.entity.Doctor;
import com.lockdoc.app.entity.Facility;
import com.lockdoc.app.entity.doctor.DoctorFacilityMapping;
import com.lockdoc.app.entity.doctor.ScheduleException;
import com.lockdoc.app.entity.op.Appointment;
import com.lockdoc.app.entity.op.DoctorSchedule;
import com.lockdoc.app.dto.op.AppointmentRescheduleRequest;
import com.lockdoc.app.dto.op.AppointmentResponse;
import com.lockdoc.app.exception.InvalidDocumentStateException;
import com.lockdoc.app.exception.ResourceNotFoundException;
import com.lockdoc.app.repository.DoctorRepository;
import com.lockdoc.app.repository.FacilityRepository;
import com.lockdoc.app.repository.doctor.DoctorFacilityMappingRepository;
import com.lockdoc.app.repository.doctor.ScheduleExceptionRepository;
import com.lockdoc.app.repository.op.AppointmentRepository;
import com.lockdoc.app.repository.op.DoctorScheduleRepository;
import com.lockdoc.app.service.op.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Availability Planner (Master Spec §8.2, screen #14) - a doctor viewing
 * their own recurring {@link DoctorSchedule} sessions expanded across a
 * date range, and either blocking part or all of a day or - the
 * drag-to-reschedule gesture on the day/week grid - moving one session's
 * occurrence to a new time, both via {@link ScheduleException}. Per §8.2,
 * this is self-service (the doctor's own schedule, no Hospital/Clinic
 * Admin approval gate) but must never silently orphan a booked patient:
 * {@link #preview} surfaces every appointment a proposed block or move
 * would affect, and {@link #create} refuses to commit unless the caller
 * supplies an explicit reschedule-or-cancel decision for every one of
 * them, applied in the same transaction as the exception itself.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class DoctorAvailabilityService {

    private static final List<String> LIVE_APPOINTMENT_STATUSES =
            List.of(Appointment.STATUS_BOOKED, Appointment.STATUS_ARRIVED, Appointment.STATUS_IN_CONSULTATION);

    private static final List<String> VALID_TYPES = List.of(
            ScheduleException.TYPE_LEAVE, ScheduleException.TYPE_SESSION_CANCEL,
            ScheduleException.TYPE_HOLIDAY, ScheduleException.TYPE_SESSION_MOVE);

    private final DoctorRepository doctorRepository;
    private final FacilityRepository facilityRepository;
    private final DoctorScheduleRepository scheduleRepository;
    private final ScheduleExceptionRepository exceptionRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorFacilityMappingRepository mappingRepository;
    private final AppointmentService appointmentService;

    /** Day/week/month grid data: this doctor's recurring sessions expanded onto every date in [from, to], overlaid with any exceptions. */
    public List<PlannerOccurrenceResponse> planner(LocalDate from, LocalDate to, Long facilityIdFilter) {
        if (to.isBefore(from)) {
            throw new InvalidDocumentStateException("`to` cannot be before `from`");
        }
        if (from.plusMonths(2).isBefore(to)) {
            throw new InvalidDocumentStateException("Requested range is too large - ask for at most about two months at a time");
        }
        Doctor doctor = currentDoctor();

        List<DoctorSchedule> schedules = scheduleRepository.findByDoctorIdAndActiveTrue(doctor.getId());
        if (facilityIdFilter != null) {
            schedules = schedules.stream().filter(s -> s.getFacility().getId().equals(facilityIdFilter)).toList();
        }
        if (schedules.isEmpty()) {
            return List.of();
        }

        List<ScheduleException> exceptions =
                exceptionRepository.findByDoctorIdAndExceptionDateBetweenOrderByExceptionDateAsc(doctor.getId(), from, to);

        List<PlannerOccurrenceResponse> out = new ArrayList<>();
        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            String weekday = date.getDayOfWeek().name();
            LocalDate d = date;
            for (DoctorSchedule s : schedules) {
                if (!s.getWeekday().equals(weekday)) continue;
                if (s.getEffectiveFrom().isAfter(d)) continue;
                if (s.getEffectiveTo() != null && s.getEffectiveTo().isBefore(d)) continue;

                ScheduleException applicable = exceptions.stream()
                        .filter(e -> e.getExceptionDate().equals(d))
                        .filter(e -> e.getDoctorSchedule() == null
                                ? e.getFacility().getId().equals(s.getFacility().getId())
                                : e.getDoctorSchedule().getId().equals(s.getId()))
                        .findFirst().orElse(null);

                LocalDateTime dayStart = d.atStartOfDay();
                long booked = appointmentRepository.countByDoctorScheduleIdAndAppointmentTsBetweenAndStatusIn(
                        s.getId(), dayStart, dayStart.plusDays(1), LIVE_APPOINTMENT_STATUSES);

                out.add(PlannerOccurrenceResponse.builder()
                        .date(d)
                        .doctorScheduleId(s.getId())
                        .facilityId(s.getFacility().getId())
                        .facilityName(s.getFacility().getName())
                        .sessionName(s.getSessionName())
                        .startTime(s.getStartTime())
                        .endTime(s.getEndTime())
                        .capacity(s.getCapacity())
                        .overbookAllowance(s.getOverbookAllowance())
                        .bookedCount((int) booked)
                        .effectiveFrom(s.getEffectiveFrom())
                        .effectiveTo(s.getEffectiveTo())
                        .blocked(applicable != null)
                        .exceptionId(applicable != null ? applicable.getId() : null)
                        .exceptionType(applicable != null ? applicable.getExceptionType() : null)
                        .exceptionReason(applicable != null ? applicable.getReason() : null)
                        .exceptionCoversWholeDay(applicable != null && applicable.getDoctorSchedule() == null)
                        .movedToStartTime(applicable != null ? applicable.getNewStartTime() : null)
                        .movedToEndTime(applicable != null ? applicable.getNewEndTime() : null)
                        .build());
            }
        }
        out.sort((a, b) -> {
            int byDate = a.getDate().compareTo(b.getDate());
            return byDate != 0 ? byDate : a.getStartTime().compareTo(b.getStartTime());
        });
        return out;
    }

    /** Appointments the proposed block would affect - call before create() so the caller can collect a decision for each one. */
    public List<AffectedAppointmentResponse> preview(ScheduleExceptionRequest request) {
        Doctor doctor = currentDoctor();
        validateRequest(doctor, request);
        return affectedAppointments(doctor, request).stream().map(AffectedAppointmentResponse::toResponse).toList();
    }

    /** Commits the block and, in the same transaction, applies the caller's reschedule/cancel decision for every currently-affected appointment (§8.2). */
    public ScheduleExceptionResponse create(ScheduleExceptionCreateRequest request) {
        Doctor doctor = currentDoctor();
        validateRequest(doctor, request);
        Facility facility = facilityRepository.getReferenceById(request.getFacilityId());

        if (request.getDoctorScheduleId() != null) {
            if (exceptionRepository.existsByDoctorScheduleIdAndExceptionDate(request.getDoctorScheduleId(), request.getExceptionDate())) {
                throw new InvalidDocumentStateException("This session is already blocked on this date");
            }
        } else if (exceptionRepository.existsByDoctorIdAndFacilityIdAndExceptionDateAndDoctorScheduleIdIsNull(
                doctor.getId(), request.getFacilityId(), request.getExceptionDate())) {
            throw new InvalidDocumentStateException("This whole day is already blocked at this facility");
        }

        List<Appointment> affected = affectedAppointments(doctor, request);

        DoctorSchedule schedule = request.getDoctorScheduleId() != null
                ? scheduleRepository.findByIdAndDoctorId(request.getDoctorScheduleId(), doctor.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Doctor schedule not found with id: " + request.getDoctorScheduleId()))
                : null;

        applyResolutions(affected, request.getResolutions(), request.getFacilityId(),
                "Doctor unavailable: " + (request.getReason() != null ? request.getReason() : request.getType()),
                rescheduled -> {
                    boolean stillOnBlockedDate = rescheduled.getAppointmentTs().toLocalDate().equals(request.getExceptionDate());
                    return schedule != null
                            ? stillOnBlockedDate && rescheduled.getDoctorScheduleId().equals(schedule.getId())
                            : stillOnBlockedDate && rescheduled.getFacilityId().equals(request.getFacilityId());
                },
                "blocking this session", "the slot being blocked");

        boolean isMove = ScheduleException.TYPE_SESSION_MOVE.equals(request.getType());
        ScheduleException exception = ScheduleException.builder()
                .doctor(doctor)
                .facility(facility)
                .doctorSchedule(schedule)
                .exceptionDate(request.getExceptionDate())
                .exceptionType(request.getType())
                .reason(request.getReason())
                .newStartTime(isMove ? request.getNewStartTime() : null)
                .newEndTime(isMove ? request.getNewEndTime() : null)
                .createdByUserId(SecurityUtils.currentUserId())
                .build();
        return ScheduleExceptionResponse.toResponse(exceptionRepository.save(exception));
    }

    /**
     * Undoes one of the doctor's own blocks or moves - the session runs on that date as usual again (§8.2).
     * Appointments already cancelled or rescheduled when the block was made stay as they are; restoring the
     * date doesn't re-book anyone. Past dates are refused: they're already part of the punctuality record.
     */
    public void restoreException(Long exceptionId) {
        Doctor doctor = currentDoctor();
        ScheduleException exception = exceptionRepository.findByIdAndDoctorId(exceptionId, doctor.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Schedule block not found with id: " + exceptionId));
        if (exception.getExceptionDate().isBefore(LocalDate.now())) {
            throw new InvalidDocumentStateException("A block on a past date can't be undone - it's already part of your punctuality record");
        }
        if (ScheduleException.TYPE_SESSION_MOVE.equals(exception.getExceptionType()) && exception.getDoctorSchedule() != null) {
            // Patients booked after the move were booked for the moved time - moving the session back
            // underneath them would silently change their appointment.
            LocalDateTime dayStart = exception.getExceptionDate().atStartOfDay();
            long booked = appointmentRepository.countByDoctorScheduleIdAndAppointmentTsBetweenAndStatusIn(
                    exception.getDoctorSchedule().getId(), dayStart, dayStart.plusDays(1), LIVE_APPOINTMENT_STATUSES);
            if (booked > 0) {
                throw new InvalidDocumentStateException(booked + " patient" + (booked == 1 ? " is" : "s are")
                        + " booked into the moved time - reschedule or cancel them before moving this session back");
            }
        }
        exceptionRepository.delete(exception);
    }

    /** Appointments that ending a recurring session after {@code lastDate} would affect - call before endSession() to collect a decision for each. */
    public List<AffectedAppointmentResponse> previewEndSession(Long doctorScheduleId, SessionEndRequest request) {
        return previewEnd(ownSchedule(currentDoctor(), doctorScheduleId), request.getLastDate());
    }

    /** Same preview for a Hospital/Clinic Admin ending any session at their own facility (Masters > Doctor Schedules). */
    public List<AffectedAppointmentResponse> previewEndSessionAtFacility(Long doctorScheduleId, SessionEndRequest request) {
        return previewEnd(facilitySchedule(doctorScheduleId), request.getLastDate());
    }

    private List<AffectedAppointmentResponse> previewEnd(DoctorSchedule schedule, LocalDate lastDate) {
        validateEnd(schedule, lastDate);
        return appointmentsAfter(schedule, lastDate).stream().map(AffectedAppointmentResponse::toResponse).toList();
    }

    /**
     * Ends one of the doctor's own recurring sessions after {@code lastDate} (§8.2). The session is trimmed, not
     * deactivated: every date up to lastDate stays in the planner and in Doctor Reports, where switching it off
     * would erase that history. A session that never started (lastDate before its first date) has no history to
     * keep, so it's removed outright. Like a block, it never silently orphans a patient - every live appointment
     * after lastDate needs a reschedule-or-cancel decision, applied in the same transaction.
     */
    public SessionEndResponse endSession(Long doctorScheduleId, SessionEndRequest request) {
        return end(ownSchedule(currentDoctor(), doctorScheduleId), request);
    }

    /**
     * Same, for a Hospital/Clinic Admin ending a session at their own facility. It replaces the old
     * "deactivate", which switched the schedule off outright: that orphaned every patient already booked
     * on it and, because reports read active schedules only, erased the session's past dates from the
     * doctor's Punctuality report.
     */
    public SessionEndResponse endSessionAtFacility(Long doctorScheduleId, SessionEndRequest request) {
        return end(facilitySchedule(doctorScheduleId), request);
    }

    private SessionEndResponse end(DoctorSchedule schedule, SessionEndRequest request) {
        LocalDate lastDate = request.getLastDate();
        validateEnd(schedule, lastDate);

        LocalDateTime removedFrom = lastDate.plusDays(1).atStartOfDay();
        applyResolutions(appointmentsAfter(schedule, lastDate), request.getResolutions(), schedule.getFacility().getId(),
                "Doctor unavailable: " + schedule.getSessionName() + " discontinued",
                rescheduled -> rescheduled.getDoctorScheduleId().equals(schedule.getId()) && !rescheduled.getAppointmentTs().isBefore(removedFrom),
                "ending this session", "a date this session no longer runs on");

        // Blocks and moves on dates the session no longer has would only clutter the facility's exception list.
        exceptionRepository.deleteAll(exceptionRepository.findByDoctorScheduleIdAndExceptionDateAfter(schedule.getId(), lastDate));

        boolean neverRan = lastDate.isBefore(schedule.getEffectiveFrom());
        if (neverRan) {
            schedule.setActive(false);
        } else {
            schedule.setEffectiveTo(lastDate);
        }
        scheduleRepository.save(schedule);

        return SessionEndResponse.builder()
                .doctorScheduleId(schedule.getId())
                .sessionName(schedule.getSessionName())
                .removed(neverRan)
                .lastDate(neverRan ? null : lastDate)
                .build();
    }

    /** The doctor's own block history, most recent first within range. */
    public List<ScheduleExceptionResponse> myExceptions(LocalDate from, LocalDate to) {
        Doctor doctor = currentDoctor();
        return exceptionRepository.findByDoctorIdAndExceptionDateBetweenOrderByExceptionDateAsc(doctor.getId(), from, to).stream()
                .map(ScheduleExceptionResponse::toResponse).toList();
    }

    /** Facility-side read visibility (§8.2 - "visible to Hospital/Clinic Admin ... immediately, not buried"). */
    public List<ScheduleExceptionResponse> forFacility(LocalDate from, LocalDate to) {
        Long facilityId = SecurityUtils.requireFacilityId();
        return exceptionRepository.findByFacilityIdAndExceptionDateBetweenOrderByExceptionDateAsc(facilityId, from, to).stream()
                .map(ScheduleExceptionResponse::toResponse).toList();
    }

    private void validateRequest(Doctor doctor, ScheduleExceptionRequest request) {
        if (!VALID_TYPES.contains(request.getType())) {
            throw new InvalidDocumentStateException("Unknown exception type: " + request.getType() + " - expected one of " + VALID_TYPES);
        }
        boolean mapped = !mappingRepository
                .findByDoctorIdAndFacilityIdAndStatusIn(doctor.getId(), request.getFacilityId(), List.of(DoctorFacilityMapping.STATUS_ACCEPTED))
                .isEmpty();
        if (!mapped) {
            throw new InvalidDocumentStateException("You are not currently mapped to this facility");
        }
        if (request.getDoctorScheduleId() != null) {
            scheduleRepository.findByIdAndDoctorId(request.getDoctorScheduleId(), doctor.getId())
                    .filter(s -> s.getFacility().getId().equals(request.getFacilityId()))
                    .orElseThrow(() -> new ResourceNotFoundException("Doctor schedule not found with id: " + request.getDoctorScheduleId()));
        }
        if (ScheduleException.TYPE_SESSION_MOVE.equals(request.getType())) {
            if (request.getDoctorScheduleId() == null) {
                throw new InvalidDocumentStateException("A move must target one specific session - whole-day moves aren't supported");
            }
            if (request.getNewStartTime() == null || request.getNewEndTime() == null) {
                throw new InvalidDocumentStateException("newStartTime and newEndTime are required to move a session");
            }
            if (!request.getNewEndTime().isAfter(request.getNewStartTime())) {
                throw new InvalidDocumentStateException("newEndTime must be after newStartTime");
            }
        }
    }

    /**
     * Applies the caller's reschedule-or-cancel decision for every appointment a block or session end affects
     * (§8.2 - "never silently orphaning"). Checked against the live affected set, not the caller's stale preview:
     * every affected appointment needs a decision, and a decision for anything outside that set is refused. The
     * callers are @Transactional, so a reschedule that lands straight back in what's being removed throws and
     * rolls back every write already applied here, leaving nothing half-committed.
     */
    private void applyResolutions(List<Appointment> affected, List<AppointmentResolutionRequest> requested, Long facilityId,
                                  String cancelReason, Predicate<AppointmentResponse> landsInRemovedSlot,
                                  String changeDescription, String removedSlotDescription) {
        List<AppointmentResolutionRequest> resolutions = requested != null ? requested : List.of();

        for (Appointment a : affected) {
            boolean covered = resolutions.stream().anyMatch(r -> r.getAppointmentId().equals(a.getId()));
            if (!covered) {
                throw new InvalidDocumentStateException(
                        "Appointment #" + a.getId() + " (" + a.getPatient().getFullName() + ") is still unresolved - reschedule or cancel it before " + changeDescription);
            }
        }

        for (AppointmentResolutionRequest r : resolutions) {
            if (affected.stream().noneMatch(a -> a.getId().equals(r.getAppointmentId()))) {
                throw new InvalidDocumentStateException("Appointment #" + r.getAppointmentId() + " isn't affected by this change");
            }
            if (AppointmentResolutionRequest.ACTION_CANCEL.equals(r.getAction())) {
                appointmentService.directCancel(r.getAppointmentId(), facilityId, cancelReason);
            } else if (AppointmentResolutionRequest.ACTION_RESCHEDULE.equals(r.getAction())) {
                if (r.getNewAppointmentTs() == null) {
                    throw new InvalidDocumentStateException("newAppointmentTs is required to reschedule appointment #" + r.getAppointmentId());
                }
                AppointmentRescheduleRequest rescheduleRequest = new AppointmentRescheduleRequest(r.getNewAppointmentTs(), r.getNewDoctorScheduleId());
                AppointmentResponse rescheduled = appointmentService.reschedule(r.getAppointmentId(), rescheduleRequest, facilityId);
                if (landsInRemovedSlot.test(rescheduled)) {
                    throw new InvalidDocumentStateException(
                            "Appointment #" + r.getAppointmentId() + " was rescheduled back into " + removedSlotDescription + " - pick a different date or session");
                }
            } else {
                throw new InvalidDocumentStateException("Unknown resolution action: " + r.getAction());
            }
        }
    }

    private DoctorSchedule ownSchedule(Doctor doctor, Long doctorScheduleId) {
        return scheduleRepository.findByIdAndDoctorId(doctorScheduleId, doctor.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor schedule not found with id: " + doctorScheduleId));
    }

    /** An admin's own facility scopes which sessions they may end - never a schedule at another facility. */
    private DoctorSchedule facilitySchedule(Long doctorScheduleId) {
        return scheduleRepository.findByIdAndFacilityId(doctorScheduleId, SecurityUtils.requireFacilityId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor schedule not found with id: " + doctorScheduleId));
    }

    /** A session can only be ended from today onward, and only earlier than it already ends. */
    private void validateEnd(DoctorSchedule schedule, LocalDate lastDate) {
        if (lastDate.isBefore(LocalDate.now().minusDays(1))) {
            throw new InvalidDocumentStateException("A session can only be ended from today onward - past dates are already part of your record");
        }
        if (schedule.getEffectiveTo() != null && !lastDate.isBefore(schedule.getEffectiveTo())) {
            throw new InvalidDocumentStateException("This session already ends on " + schedule.getEffectiveTo());
        }
    }

    /** Live appointments on this session from the day after lastDate onward. */
    private List<Appointment> appointmentsAfter(DoctorSchedule schedule, LocalDate lastDate) {
        return appointmentRepository.findByDoctorScheduleIdAndAppointmentTsGreaterThanEqualAndStatusInOrderByAppointmentTsAsc(
                schedule.getId(), lastDate.plusDays(1).atStartOfDay(), LIVE_APPOINTMENT_STATUSES);
    }

    private List<Appointment> affectedAppointments(Doctor doctor, ScheduleExceptionRequest request) {
        LocalDateTime dayStart = request.getExceptionDate().atStartOfDay();
        List<Appointment> dayAppointments = appointmentRepository.findByDoctorIdAndFacilityIdAndAppointmentTsBetweenAndStatusIn(
                doctor.getId(), request.getFacilityId(), dayStart, dayStart.plusDays(1), LIVE_APPOINTMENT_STATUSES);
        if (request.getDoctorScheduleId() == null) {
            return dayAppointments;
        }
        return dayAppointments.stream()
                .filter(a -> a.getDoctorSchedule().getId().equals(request.getDoctorScheduleId()))
                .toList();
    }

    private Doctor currentDoctor() {
        Long userId = SecurityUtils.currentUserId();
        return doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No Doctor profile for the current user"));
    }
}
