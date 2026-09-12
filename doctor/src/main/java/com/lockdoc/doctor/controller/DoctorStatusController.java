package com.lockdoc.doctor.controller;

import com.lockdoc.doctor.dto.DoctorStatusRequest;
import com.lockdoc.doctor.dto.DoctorStatusResponse;
import com.lockdoc.doctor.dto.DoctorStatusTodayResponse;
import com.lockdoc.doctor.service.DoctorStatusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Doctor's own live status (Master Spec §8.3) — build order step 4's named deliverable. */
@RestController
@RequestMapping("/doctor/status")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('DOCTOR_STATUS_UPDATE')")
public class DoctorStatusController {

    private final DoctorStatusService statusService;

    @PostMapping
    public ResponseEntity<DoctorStatusResponse> setStatus(@Valid @RequestBody DoctorStatusRequest request) {
        return ResponseEntity.ok(statusService.setStatus(request));
    }

    @GetMapping("/today")
    public ResponseEntity<List<DoctorStatusTodayResponse>> myStatusToday() {
        return ResponseEntity.ok(statusService.myStatusToday());
    }
}
