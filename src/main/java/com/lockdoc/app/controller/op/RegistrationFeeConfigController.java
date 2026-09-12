package com.lockdoc.app.controller.op;

import com.lockdoc.app.dto.op.RegistrationFeeConfigRequest;
import com.lockdoc.app.dto.op.RegistrationFeeConfigResponse;
import com.lockdoc.app.service.op.RegistrationFeeConfigService;
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
