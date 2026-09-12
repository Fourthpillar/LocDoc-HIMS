package com.lockdoc.app.controller;

import com.lockdoc.app.dto.FacilityModulesRequest;
import com.lockdoc.app.dto.FacilityRequest;
import com.lockdoc.app.dto.FacilityResponse;
import com.lockdoc.app.dto.PageResponse;
import com.lockdoc.app.service.FacilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Super Admin's facility onboarding queue (Master Spec §10, build order
 * §16 step 2) - cross-facility by design, gated by FACILITY_MANAGE /
 * FACILITY_VERIFY (V10), both mapped only to SUPER_ADMIN.
 */
@RestController
@RequestMapping("/platform/facilities")
@RequiredArgsConstructor
public class FacilityController {

    private final FacilityService facilityService;

    @GetMapping
    @PreAuthorize("hasAuthority('FACILITY_MANAGE')")
    public ResponseEntity<PageResponse<FacilityResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(facilityService.list(page, size, search));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('FACILITY_MANAGE')")
    public ResponseEntity<FacilityResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(facilityService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('FACILITY_MANAGE')")
    public ResponseEntity<FacilityResponse> register(@Valid @RequestBody FacilityRequest request) {
        return ResponseEntity.ok(facilityService.register(request));
    }

    @PostMapping("/{id}/verify")
    @PreAuthorize("hasAuthority('FACILITY_VERIFY')")
    public ResponseEntity<FacilityResponse> verify(@PathVariable Long id) {
        return ResponseEntity.ok(facilityService.verify(id));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('FACILITY_VERIFY')")
    public ResponseEntity<FacilityResponse> reject(@PathVariable Long id) {
        return ResponseEntity.ok(facilityService.reject(id));
    }

    @PostMapping("/{id}/suspend")
    @PreAuthorize("hasAuthority('FACILITY_VERIFY')")
    public ResponseEntity<FacilityResponse> suspend(@PathVariable Long id) {
        return ResponseEntity.ok(facilityService.suspend(id));
    }

    @PostMapping("/{id}/reinstate")
    @PreAuthorize("hasAuthority('FACILITY_VERIFY')")
    public ResponseEntity<FacilityResponse> reinstate(@PathVariable Long id) {
        return ResponseEntity.ok(facilityService.reinstate(id));
    }

    @PatchMapping("/{id}/modules")
    @PreAuthorize("hasAuthority('FACILITY_VERIFY')")
    public ResponseEntity<FacilityResponse> updateModules(@PathVariable Long id, @Valid @RequestBody FacilityModulesRequest request) {
        return ResponseEntity.ok(facilityService.updateModules(id, request.getActiveModules()));
    }
}
