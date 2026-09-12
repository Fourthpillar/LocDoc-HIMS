package com.lockdoc.app.controller.op;

import com.lockdoc.app.dto.op.*;
import com.lockdoc.app.service.op.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/** Master Spec §17.7 screens #5 (booking), #10 (waitlist). */
@RestController
@RequestMapping("/op/appointments")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('APPOINTMENT_MANAGE')")
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    public ResponseEntity<AppointmentResponse> book(@Valid @RequestBody AppointmentRequest request) {
        return ResponseEntity.ok(appointmentService.book(request));
    }

    @PostMapping("/{id}/reschedule")
    public ResponseEntity<AppointmentResponse> reschedule(@PathVariable Long id, @Valid @RequestBody AppointmentRescheduleRequest request) {
        return ResponseEntity.ok(appointmentService.reschedule(id, request));
    }

    @GetMapping("/today")
    public ResponseEntity<List<AppointmentResponse>> listForFacilityToday() {
        return ResponseEntity.ok(appointmentService.listForFacilityToday());
    }

    @PostMapping("/waitlist")
    public ResponseEntity<WaitlistResponse> joinWaitlist(@Valid @RequestBody WaitlistJoinRequest request) {
        return ResponseEntity.ok(appointmentService.joinWaitlist(request));
    }

    @GetMapping("/waitlist")
    public ResponseEntity<List<WaitlistResponse>> listWaitlist(
            @RequestParam Long doctorScheduleId, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate sessionDate) {
        return ResponseEntity.ok(appointmentService.listWaitlist(doctorScheduleId, sessionDate));
    }

    @PostMapping("/waitlist/{id}/promote")
    public ResponseEntity<AppointmentResponse> promoteFromWaitlist(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.promoteFromWaitlist(id));
    }
}
