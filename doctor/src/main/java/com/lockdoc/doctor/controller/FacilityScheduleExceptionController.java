package com.lockdoc.doctor.controller;

import com.lockdoc.doctor.dto.ScheduleExceptionResponse;
import com.lockdoc.doctor.service.DoctorAvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * Facility-side read visibility onto doctors' schedule blocks (Master
 * Spec §8.2 - "visible to Hospital/Clinic Admin in the facility's day
 * list immediately, not buried"). Reuses DOCTOR_SCHEDULE_MANAGE rather
 * than a new right, since Hospital Admin already holds that for the
 * recurring-session templates these exceptions block.
 */
@RestController
@RequestMapping("/facility/schedule-exceptions")
@RequiredArgsConstructor
public class FacilityScheduleExceptionController {

    private final DoctorAvailabilityService availabilityService;

    @GetMapping
    @PreAuthorize("hasAuthority('DOCTOR_SCHEDULE_MANAGE')")
    public ResponseEntity<List<ScheduleExceptionResponse>> forFacility(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(availabilityService.forFacility(from, to));
    }
}
