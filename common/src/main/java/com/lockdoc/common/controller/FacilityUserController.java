package com.lockdoc.common.controller;

import com.lockdoc.common.dto.FacilityUserRequest;
import com.lockdoc.common.dto.FacilityUserResponse;
import com.lockdoc.common.service.FacilityUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Manage Users (Master Spec §17.7 #33a) - Receptionist accounts, own facility only. */
@RestController
@RequestMapping("/facility/users")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('FACILITY_USER_MANAGE')")
public class FacilityUserController {

    private final FacilityUserService facilityUserService;

    @GetMapping
    public ResponseEntity<List<FacilityUserResponse>> list() {
        return ResponseEntity.ok(facilityUserService.list());
    }

    @PostMapping
    public ResponseEntity<FacilityUserResponse> create(@Valid @RequestBody FacilityUserRequest request) {
        return ResponseEntity.ok(facilityUserService.create(request));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<FacilityUserResponse> deactivate(@PathVariable Long id) {
        return ResponseEntity.ok(facilityUserService.setEnabled(id, false));
    }

    @PatchMapping("/{id}/reactivate")
    public ResponseEntity<FacilityUserResponse> reactivate(@PathVariable Long id) {
        return ResponseEntity.ok(facilityUserService.setEnabled(id, true));
    }

    @PostMapping("/{id}/require-password-change")
    public ResponseEntity<FacilityUserResponse> requirePasswordChange(@PathVariable Long id) {
        return ResponseEntity.ok(facilityUserService.requirePasswordChange(id));
    }
}
