package com.lockdoc.outpatient.controller;

import com.lockdoc.outpatient.dto.PatientRegistrationRequest;
import com.lockdoc.outpatient.dto.PatientRegistrationResponse;
import com.lockdoc.outpatient.service.PatientRegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** Master Spec §17.7 screen #4. */
@RestController
@RequestMapping("/op/registrations")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('PATIENT_MANAGE')")
public class PatientRegistrationController {

    private final PatientRegistrationService registrationService;

    @PostMapping
    public ResponseEntity<PatientRegistrationResponse> register(@Valid @RequestBody PatientRegistrationRequest request) {
        return ResponseEntity.ok(registrationService.register(request));
    }

    @GetMapping("/latest")
    public ResponseEntity<PatientRegistrationResponse> latestForPatient(@RequestParam Long patientId) {
        return ResponseEntity.ok(registrationService.latestForPatient(patientId));
    }
}
