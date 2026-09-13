package com.lockdoc.outpatient.controller;

import com.lockdoc.outpatient.dto.RegistrationFeeConfigRequest;
import com.lockdoc.outpatient.dto.RegistrationFeeConfigResponse;
import com.lockdoc.outpatient.service.RegistrationFeeConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/op/registration-fee-config")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('REGISTRATION_FEE_CONFIG_MANAGE')")
public class RegistrationFeeConfigController {

    private final RegistrationFeeConfigService configService;

    @GetMapping
    public ResponseEntity<RegistrationFeeConfigResponse> getMine() {
        return ResponseEntity.ok(configService.getMine());
    }

    @PutMapping
    public ResponseEntity<RegistrationFeeConfigResponse> upsert(@Valid @RequestBody RegistrationFeeConfigRequest request) {
        return ResponseEntity.ok(configService.upsert(request));
    }
}
