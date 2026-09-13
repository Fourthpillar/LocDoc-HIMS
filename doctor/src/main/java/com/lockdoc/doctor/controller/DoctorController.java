package com.lockdoc.doctor.controller;

import com.lockdoc.doctor.dto.DoctorRegisterRequest;
import com.lockdoc.doctor.dto.DoctorResponse;
import com.lockdoc.common.dto.PageResponse;
import com.lockdoc.doctor.service.DoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Doctor self-registration + Super Admin verification (Master Spec §10).
 * "/register" is listed under app.security.jwt.excluded-urls - a doctor
 * signing up has no JWT yet to present, same shape as "/auth/login".
 * Every other action here is Super Admin's onboarding queue (DOCTOR_VERIFY,
 * V11).
 */
@RestController
@RequestMapping("/platform/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @PostMapping("/register")
    public ResponseEntity<DoctorResponse> register(@Valid @RequestBody DoctorRegisterRequest request) {
        return ResponseEntity.ok(doctorService.register(request));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('DOCTOR_VERIFY')")
    public ResponseEntity<PageResponse<DoctorResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(doctorService.list(page, size, search));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('DOCTOR_VERIFY')")
    public ResponseEntity<DoctorResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(doctorService.get(id));
    }

    @PostMapping("/{id}/verify")
    @PreAuthorize("hasAuthority('DOCTOR_VERIFY')")
    public ResponseEntity<DoctorResponse> verify(@PathVariable Long id) {
        return ResponseEntity.ok(doctorService.verify(id));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('DOCTOR_VERIFY')")
    public ResponseEntity<DoctorResponse> reject(@PathVariable Long id) {
        return ResponseEntity.ok(doctorService.reject(id));
    }
}
