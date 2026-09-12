package com.lockdoc.app.controller.doctor;

import com.lockdoc.app.dto.doctor.ConsultationRateRequest;
import com.lockdoc.app.dto.doctor.ConsultationRateResponse;
import com.lockdoc.app.service.doctor.ConsultationRateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Doctor-proposed consultation fee (Master Spec §8.6) — propose half only,
 * per build order step 4; the Hospital/Clinic Admin approve endpoint
 * ships with step 5.
 */
@RestController
@RequestMapping("/doctor/consultation-rates")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('CONSULTATION_FEE_PROPOSE')")
public class ConsultationRateController {

    private final ConsultationRateService rateService;

    @PostMapping
    public ResponseEntity<ConsultationRateResponse> propose(@Valid @RequestBody ConsultationRateRequest request) {
        return ResponseEntity.ok(rateService.propose(request));
    }

    @GetMapping
    public ResponseEntity<List<ConsultationRateResponse>> myProposals(@RequestParam(required = false) Long facilityId) {
        return ResponseEntity.ok(rateService.myProposals(facilityId));
    }
}
