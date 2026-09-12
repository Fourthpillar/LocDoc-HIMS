package com.lockdoc.app.controller.doctor;

import com.lockdoc.app.dto.doctor.DoctorStatusRequest;
import com.lockdoc.app.dto.doctor.DoctorStatusResponse;
import com.lockdoc.app.dto.doctor.DoctorStatusTodayResponse;
import com.lockdoc.app.service.doctor.DoctorStatusService;
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
