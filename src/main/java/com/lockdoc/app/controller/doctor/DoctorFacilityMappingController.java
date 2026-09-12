package com.lockdoc.app.controller.doctor;

import com.lockdoc.app.dto.doctor.DoctorFacilityMappingResponse;
import com.lockdoc.app.dto.doctor.ConsultationHourRequest;
import com.lockdoc.app.dto.doctor.FacilityMappingRequestRequest;
import com.lockdoc.app.dto.doctor.FacilitySearchResult;
import com.lockdoc.app.service.doctor.DoctorFacilityMappingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Doctor's own side of §8.1 — request a facility, and respond to invites addressed to them. */
@RestController
@RequestMapping("/doctor/facility-mappings")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('DOCTOR_FACILITY_MAPPING_MANAGE')")
public class DoctorFacilityMappingController {

    private final DoctorFacilityMappingService mappingService;

    @GetMapping
    public ResponseEntity<List<DoctorFacilityMappingResponse>> myMappings() {
        return ResponseEntity.ok(mappingService.myMappings());
    }

    /** Hospitals/clinics this doctor could ask to practise at — verified and active only. */
    @GetMapping("/search-facilities")
    public ResponseEntity<List<FacilitySearchResult>> searchFacilities(@RequestParam(required = false) String search) {
        return ResponseEntity.ok(mappingService.searchRequestableFacilities(search));
    }

    @PostMapping
    public ResponseEntity<DoctorFacilityMappingResponse> request(@Valid @RequestBody FacilityMappingRequestRequest request) {
        return ResponseEntity.ok(mappingService.requestFacility(request));
    }

    /** The doctor's own statement of when they consult at this facility (§8.2) — replaces whatever was stated before. */
    @PutMapping("/{id}/consultation-hours")
    public ResponseEntity<DoctorFacilityMappingResponse> setConsultationHours(@PathVariable Long id,
                                                                             @Valid @RequestBody List<ConsultationHourRequest> hours) {
        return ResponseEntity.ok(mappingService.setConsultationHours(id, hours));
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<DoctorFacilityMappingResponse> accept(@PathVariable Long id) {
        return ResponseEntity.ok(mappingService.accept(id));
    }

    @PostMapping("/{id}/decline")
    public ResponseEntity<DoctorFacilityMappingResponse> decline(@PathVariable Long id) {
        return ResponseEntity.ok(mappingService.decline(id));
    }

    @PostMapping("/{id}/end")
    public ResponseEntity<DoctorFacilityMappingResponse> end(@PathVariable Long id) {
        return ResponseEntity.ok(mappingService.end(id));
    }
}
