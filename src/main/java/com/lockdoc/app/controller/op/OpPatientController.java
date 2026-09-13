package com.lockdoc.app.controller.op;

import com.lockdoc.app.dto.PageResponse;
import com.lockdoc.app.dto.op.PatientClinicalVisitResponse;
import com.lockdoc.app.dto.pharmacy.PatientRequest;
import com.lockdoc.app.dto.pharmacy.PatientResponse;
import com.lockdoc.app.service.op.PatientClinicalHistoryService;
import com.lockdoc.app.service.pharmacy.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * OP's own gated view of the shared Patient identity (Master Spec §6
 * invariant 7 - one Patient row per facility serves both OP and
 * Pharmacy). Delegates to the same {@link PatientService} the pharmacy
 * module already uses under {@code PHARMACY_PATIENT_MANAGE} - this
 * controller exists only to give Receptionist/Hospital-Admin their own
 * URL namespace and right ({@code PATIENT_MANAGE}), not a second
 * implementation.
 */
@RestController
@RequestMapping("/op/patients")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('PATIENT_MANAGE')")
public class OpPatientController {

    private final PatientService patientService;
    private final PatientClinicalHistoryService clinicalHistoryService;

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

    /**
     * What each past consultation recorded - diagnosis, history, plan and the medicines
     * prescribed. Its own right, because reading a clinical note is a different decision
     * from editing a patient's name and address.
     */
    @GetMapping("/{id}/clinical-history")
    @PreAuthorize("hasAuthority('PATIENT_CLINICAL_VIEW')")
    public ResponseEntity<List<PatientClinicalVisitResponse>> clinicalHistory(@PathVariable Long id) {
        return ResponseEntity.ok(clinicalHistoryService.forPatient(id));
    }

    @PostMapping
    public ResponseEntity<PatientResponse> create(@Valid @RequestBody PatientRequest request) {
        return ResponseEntity.ok(patientService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PatientResponse> update(@PathVariable Long id, @Valid @RequestBody PatientRequest request) {
        return ResponseEntity.ok(patientService.update(id, request));
    }
}
