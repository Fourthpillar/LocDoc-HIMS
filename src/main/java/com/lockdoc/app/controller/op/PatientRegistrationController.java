package com.lockdoc.app.controller.op;

import com.lockdoc.app.dto.op.PatientRegistrationRequest;
import com.lockdoc.app.dto.op.PatientRegistrationResponse;
import com.lockdoc.app.service.op.PatientRegistrationService;
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
