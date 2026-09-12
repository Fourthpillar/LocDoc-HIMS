package com.lockdoc.app.controller;

import com.lockdoc.app.dto.DoctorProfileResponse;
import com.lockdoc.app.dto.DoctorProfileUpdateRequest;
import com.lockdoc.app.service.DoctorProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** Doctor profile, self-service (Master Spec §8.9, screen #19). */
@RestController
@RequestMapping("/doctor/profile")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('DOCTOR_PROFILE_MANAGE')")
public class DoctorProfileController {

    private final DoctorProfileService doctorProfileService;

    @GetMapping
    public ResponseEntity<DoctorProfileResponse> getMine() {
        return ResponseEntity.ok(doctorProfileService.getMine());
    }

    @PutMapping
    public ResponseEntity<DoctorProfileResponse> updateMine(@Valid @RequestBody DoctorProfileUpdateRequest request) {
        return ResponseEntity.ok(doctorProfileService.updateMine(request));
    }
}
