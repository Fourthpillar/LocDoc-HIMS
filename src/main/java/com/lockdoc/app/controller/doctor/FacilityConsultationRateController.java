package com.lockdoc.app.controller.doctor;

import com.lockdoc.app.dto.doctor.ConsultationRateRejectRequest;
import com.lockdoc.app.dto.doctor.ConsultationRateResponse;
import com.lockdoc.app.service.doctor.ConsultationRateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Hospital/Clinic Admin's approve/reject of a doctor-proposed
 * consultation fee (Master Spec §8.6) — completes the flow build order
 * step 4 started (propose only) and step 5 promised ("approve half of
 * the flow step 4 already has proposals waiting for").
 */
@RestController
@RequestMapping("/facility/consultation-rates")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('CONSULTATION_FEE_APPROVE')")
public class FacilityConsultationRateController {

    private final ConsultationRateService rateService;

    @GetMapping("/pending")
    public ResponseEntity<List<ConsultationRateResponse>> pending() {
        return ResponseEntity.ok(rateService.pendingForFacility());
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ConsultationRateResponse> approve(@PathVariable Long id) {
        return ResponseEntity.ok(rateService.approve(id));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ConsultationRateResponse> reject(@PathVariable Long id, @Valid @RequestBody ConsultationRateRejectRequest request) {
        return ResponseEntity.ok(rateService.reject(id, request));
    }
}
