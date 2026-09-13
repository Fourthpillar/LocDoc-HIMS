package com.lockdoc.outpatient.controller;

import com.lockdoc.common.dto.PageResponse;
import com.lockdoc.outpatient.dto.PatientRequest;
import com.lockdoc.outpatient.dto.PatientResponse;
import com.lockdoc.outpatient.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Patient master data (Master Spec §6 invariant 7 - one Patient row per
 * facility), gated by {@code PATIENT_MANAGE}.
 */
@RestController
@RequestMapping("/op/patients")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('PATIENT_MANAGE')")
public class OpPatientController {

    private final PatientService patientService;

    @GetMapping
    public ResponseEntity<PageResponse<PatientResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(patientService.list(page, size, search));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PatientResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.get(id));
    }

    @PostMapping
    public ResponseEntity<PatientResponse> create(@Valid @RequestBody PatientRequest request) {
        return ResponseEntity.ok(patientService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PatientResponse> update(@PathVariable Long id, @Valid @RequestBody PatientRequest request) {
        return ResponseEntity.ok(patientService.update(id, request));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        patientService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
