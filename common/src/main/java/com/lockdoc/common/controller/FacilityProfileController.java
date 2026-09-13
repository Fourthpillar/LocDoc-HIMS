package com.lockdoc.common.controller;

import com.lockdoc.common.dto.FacilityProfileRequest;
import com.lockdoc.common.dto.FacilityResponse;
import com.lockdoc.common.service.FacilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** Facility self-profile (Master Spec §17.7 #33b) - own facility only, distinct from Super Admin's cross-facility FacilityController. */
@RestController
@RequestMapping("/facility/profile")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('FACILITY_PROFILE_MANAGE')")
public class FacilityProfileController {

    private final FacilityService facilityService;

    @GetMapping
    public ResponseEntity<FacilityResponse> getMine() {
        return ResponseEntity.ok(facilityService.getMine());
    }

    @PutMapping
    public ResponseEntity<FacilityResponse> updateMine(@Valid @RequestBody FacilityProfileRequest request) {
        return ResponseEntity.ok(facilityService.updateMine(request));
    }
}
