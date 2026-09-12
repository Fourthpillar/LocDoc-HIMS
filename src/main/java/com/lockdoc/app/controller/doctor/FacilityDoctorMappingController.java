package com.lockdoc.app.controller.doctor;

import com.lockdoc.app.dto.DoctorSearchResult;
import com.lockdoc.app.dto.doctor.DoctorFacilityMappingResponse;
import com.lockdoc.app.dto.doctor.FacilityMappingInviteRequest;
import com.lockdoc.app.service.doctor.DoctorFacilityMappingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Hospital/Clinic Admin's side of §8.1 — invite a verified doctor, respond
 * to doctors who requested this facility, and end an accepted mapping. All
 * at their own facility only ({@code SecurityUtils.requireFacilityId()}
 * inside the service).
 */
@RestController
@RequestMapping("/facility/doctor-mappings")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('FACILITY_DOCTOR_MAPPING_MANAGE')")
public class FacilityDoctorMappingController {

    private final DoctorFacilityMappingService mappingService;

    @GetMapping
    public ResponseEntity<List<DoctorFacilityMappingResponse>> facilityMappings() {
        return ResponseEntity.ok(mappingService.facilityMappings());
    }

    @GetMapping("/search-doctors")
    public ResponseEntity<List<DoctorSearchResult>> searchDoctors(@RequestParam(required = false) String search) {
        return ResponseEntity.ok(mappingService.searchVerifiedDoctors(search));
    }

    @PostMapping
    public ResponseEntity<DoctorFacilityMappingResponse> invite(@Valid @RequestBody FacilityMappingInviteRequest request) {
        return ResponseEntity.ok(mappingService.invite(request));
    }

    /** Approving a request a doctor raised for this facility. */
    @PostMapping("/{id}/approve")
    public ResponseEntity<DoctorFacilityMappingResponse> approve(@PathVariable Long id) {
        return ResponseEntity.ok(mappingService.approveRequest(id));
    }

    /** Declining a doctor's request, or withdrawing an invite this facility sent. */
    @PostMapping("/{id}/decline")
    public ResponseEntity<DoctorFacilityMappingResponse> decline(@PathVariable Long id) {
        return ResponseEntity.ok(mappingService.declineRequest(id));
    }

    @PostMapping("/{id}/end")
    public ResponseEntity<DoctorFacilityMappingResponse> end(@PathVariable Long id) {
        return ResponseEntity.ok(mappingService.end(id));
    }
}
