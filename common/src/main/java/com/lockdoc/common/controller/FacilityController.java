package com.lockdoc.common.controller;

import com.lockdoc.common.dto.FacilityResponse;
import com.lockdoc.common.repository.FacilityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * The facilities on the platform, for the one role that works across all of them.
 *
 * A user who belongs to a facility never needs this — their own is implicit in every
 * request. Super Admin belongs to none, so anything they do that must land in a
 * particular facility (inviting a doctor into one, setting a policy for one) needs a
 * list to choose from first.
 */
@RestController
@RequestMapping("/platform/facilities")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('SYSTEM_ADMIN')")
public class FacilityController {

    private final FacilityRepository facilityRepository;

    @GetMapping
    public ResponseEntity<List<FacilityResponse>> listActive() {
        return ResponseEntity.ok(facilityRepository.findByActiveTrueOrderByNameAsc().stream()
                .map(FacilityResponse::toResponse)
                .toList());
    }
}
