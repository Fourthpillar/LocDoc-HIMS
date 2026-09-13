package com.lockdoc.outpatient.controller.dataprotection;

import com.lockdoc.outpatient.dto.dataprotection.ConsentRequest;
import com.lockdoc.outpatient.dto.dataprotection.ConsentResponse;
import com.lockdoc.outpatient.service.dataprotection.ConsentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Master Spec §18.2, screen #38a - own facility only. */
@RestController
@RequestMapping("/facility/consents")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('PATIENT_CONSENT_MANAGE')")
public class ConsentController {

    private final ConsentService consentService;

    @PostMapping
    public ResponseEntity<ConsentResponse> capture(@Valid @RequestBody ConsentRequest request) {
        return ResponseEntity.ok(consentService.capture(request));
    }

    @GetMapping
    public ResponseEntity<List<ConsentResponse>> historyForPatient(@RequestParam Long patientId) {
        return ResponseEntity.ok(consentService.historyForPatient(patientId));
    }
}
