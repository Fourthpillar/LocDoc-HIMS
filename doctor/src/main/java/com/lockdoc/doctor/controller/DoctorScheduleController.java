package com.lockdoc.doctor.controller;

import com.lockdoc.doctor.dto.AffectedAppointmentResponse;
import com.lockdoc.doctor.dto.SessionEndRequest;
import com.lockdoc.doctor.dto.SessionEndResponse;
import com.lockdoc.outpatient.dto.DoctorScheduleRequest;
import com.lockdoc.outpatient.dto.DoctorScheduleResponse;
import com.lockdoc.doctor.service.DoctorAvailabilityService;
import com.lockdoc.outpatient.service.DoctorScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/op/doctor-schedules")
@RequiredArgsConstructor
public class DoctorScheduleController {

    private final DoctorScheduleService scheduleService;
    private final DoctorAvailabilityService availabilityService;

    // Read is available to APPOINTMENT_MANAGE (Receptionist/Hospital Admin
    // need it to book) as well as DOCTOR_SCHEDULE_MANAGE (Hospital Admin's
    // own masters screen) - either right is enough to list.
    @GetMapping
    @PreAuthorize("hasAnyAuthority('APPOINTMENT_MANAGE', 'DOCTOR_SCHEDULE_MANAGE')")
    public ResponseEntity<List<DoctorScheduleResponse>> listForFacility() {
        return ResponseEntity.ok(scheduleService.listForFacility());
    }

    // Create/end: Hospital/Clinic Admin (any mapped doctor at their facility) OR
    // the doctor themselves, self-scheduling (§8.2's "restored here" drag-to-reschedule
    // scope extends naturally to defining the sessions in the first place - a doctor who
    // can already block/move their own occurrences had no way to ever create one).
    @PostMapping
    @PreAuthorize("hasAnyAuthority('DOCTOR_SCHEDULE_MANAGE', 'DOCTOR_AVAILABILITY_MANAGE')")
    public ResponseEntity<DoctorScheduleResponse> create(@Valid @RequestBody DoctorScheduleRequest request) {
        return ResponseEntity.ok(scheduleService.create(request));
    }

    // Ending a session replaced deactivating it: a schedule switched off orphaned everyone booked on it
    // and, since reports read active schedules only, took the doctor's past sessions out of Punctuality
    // with it. Both run through the Availability Planner's own end-session flow (§8.2).
    @PostMapping("/{id}/end/preview")
    @PreAuthorize("hasAuthority('DOCTOR_SCHEDULE_MANAGE')")
    public ResponseEntity<List<AffectedAppointmentResponse>> previewEnd(@PathVariable Long id, @Valid @RequestBody SessionEndRequest request) {
        return ResponseEntity.ok(availabilityService.previewEndSessionAtFacility(id, request));
    }

    @PostMapping("/{id}/end")
    @PreAuthorize("hasAuthority('DOCTOR_SCHEDULE_MANAGE')")
    public ResponseEntity<SessionEndResponse> end(@PathVariable Long id, @Valid @RequestBody SessionEndRequest request) {
        return ResponseEntity.ok(availabilityService.endSessionAtFacility(id, request));
    }
}
