package com.lockdoc.outpatient.controller;

import com.lockdoc.outpatient.dto.BillResponse;
import com.lockdoc.outpatient.dto.ConsultationRatingRequest;
import com.lockdoc.outpatient.dto.ConsultationRatingResponse;
import com.lockdoc.outpatient.dto.OpVisitRequest;
import com.lockdoc.outpatient.dto.OpVisitResponse;
import com.lockdoc.outpatient.service.OpVisitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Master Spec §17.7 screens #6 (day list feed) and vitals capture within #4/#5. */
@RestController
@RequestMapping("/op/visits")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('OP_VISIT_MANAGE')")
public class OpVisitController {

    private final OpVisitService visitService;

    @PostMapping
    public ResponseEntity<OpVisitResponse> arrive(@Valid @RequestBody OpVisitRequest request) {
        return ResponseEntity.ok(visitService.arrive(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OpVisitResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(visitService.get(id));
    }

    @GetMapping("/today")
    public ResponseEntity<List<OpVisitResponse>> listForFacilityToday() {
        return ResponseEntity.ok(visitService.listForFacilityToday());
    }

    /** One patient's visit history (Patient Record, §17.7 #3) - /today is the day list's feed, this is the patient's own. */
    @GetMapping("/by-patient")
    public ResponseEntity<List<OpVisitResponse>> listForPatient(@RequestParam Long patientId) {
        return ResponseEntity.ok(visitService.listForPatient(patientId));
    }

    @PostMapping("/{id}/start-consultation")
    public ResponseEntity<OpVisitResponse> startConsultation(@PathVariable Long id) {
        return ResponseEntity.ok(visitService.startConsultation(id));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<OpVisitResponse> complete(@PathVariable Long id) {
        return ResponseEntity.ok(visitService.complete(id));
    }

    /**
     * What this patient would owe if they saw this doctor — read-only, used before
     * booking (§7.4). `when` is the appointment's own date and time; omit it to quote
     * for right now (a walk-in).
     */
    @GetMapping("/charge-preview")
    public ResponseEntity<com.lockdoc.outpatient.dto.VisitChargePreviewResponse> chargePreview(
            @RequestParam Long patientId,
            @RequestParam Long doctorId,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime when) {
        return ResponseEntity.ok(visitService.previewCharges(patientId, doctorId, when));
    }

    @PostMapping("/{id}/bill-consultation")
    @PreAuthorize("hasAuthority('OP_BILLING_MANAGE')")
    public ResponseEntity<BillResponse> billConsultation(@PathVariable Long id) {
        return ResponseEntity.ok(visitService.billConsultation(id));
    }

    /** Front-desk patient-satisfaction capture (Master Spec §17.7 #18) - only once the visit is COMPLETED. */
    @PostMapping("/{id}/rating")
    public ResponseEntity<ConsultationRatingResponse> rate(@PathVariable Long id, @Valid @RequestBody ConsultationRatingRequest request) {
        return ResponseEntity.ok(visitService.rate(id, request));
    }

    @GetMapping("/{id}/rating")
    public ResponseEntity<ConsultationRatingResponse> getRating(@PathVariable Long id) {
        return ResponseEntity.ok(visitService.getRating(id));
    }
}
