package com.lockdoc.app.controller.doctor;

import com.lockdoc.app.dto.doctor.*;
import com.lockdoc.app.service.doctor.DoctorAvailabilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/** Availability Planner, doctor side (Master Spec §8.2, screen #14) - self-service, no approval gate. */
@RestController
@RequestMapping("/doctor/availability")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('DOCTOR_AVAILABILITY_MANAGE')")
public class DoctorAvailabilityController {

    private final DoctorAvailabilityService availabilityService;

    @GetMapping
    public ResponseEntity<List<PlannerOccurrenceResponse>> planner(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Long facilityId) {
        return ResponseEntity.ok(availabilityService.planner(from, to, facilityId));
    }

    @PostMapping("/exceptions/preview")
    public ResponseEntity<List<AffectedAppointmentResponse>> preview(@Valid @RequestBody ScheduleExceptionRequest request) {
        return ResponseEntity.ok(availabilityService.preview(request));
    }

    @PostMapping("/exceptions")
    public ResponseEntity<ScheduleExceptionResponse> create(@Valid @RequestBody ScheduleExceptionCreateRequest request) {
        return ResponseEntity.ok(availabilityService.create(request));
    }

    /** Undo a block or move on today or a later date - the session runs that day as usual again. */
    @DeleteMapping("/exceptions/{id}")
    public ResponseEntity<Void> restore(@PathVariable Long id) {
        availabilityService.restoreException(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/sessions/{doctorScheduleId}/end/preview")
    public ResponseEntity<List<AffectedAppointmentResponse>> previewEndSession(
            @PathVariable Long doctorScheduleId, @Valid @RequestBody SessionEndRequest request) {
        return ResponseEntity.ok(availabilityService.previewEndSession(doctorScheduleId, request));
    }

    /** End one of the doctor's own recurring sessions after a date, resolving every appointment booked beyond it. */
    @PostMapping("/sessions/{doctorScheduleId}/end")
    public ResponseEntity<SessionEndResponse> endSession(@PathVariable Long doctorScheduleId, @Valid @RequestBody SessionEndRequest request) {
        return ResponseEntity.ok(availabilityService.endSession(doctorScheduleId, request));
    }

    @GetMapping("/exceptions")
    public ResponseEntity<List<ScheduleExceptionResponse>> myExceptions(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(availabilityService.myExceptions(from, to));
    }
}
