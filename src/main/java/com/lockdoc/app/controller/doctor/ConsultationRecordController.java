package com.lockdoc.app.controller.doctor;

import com.lockdoc.app.dto.doctor.*;
import com.lockdoc.app.dto.op.CancellationResponse;
import com.lockdoc.app.service.doctor.ConsultationRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Consultation Workspace (Master Spec §17.7 screen #16, build order step 6). */
@RestController
@RequestMapping("/doctor/consultations")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('CONSULTATION_RECORD_MANAGE')")
public class ConsultationRecordController {

    private final ConsultationRecordService consultationRecordService;

    @GetMapping("/queue")
    public ResponseEntity<List<ConsultationQueueEntry>> myQueueToday() {
        return ResponseEntity.ok(consultationRecordService.myQueueToday());
    }

    @GetMapping("/history")
    public ResponseEntity<List<ConsultationHistoryEntry>> priorHistory(@RequestParam Long patientId) {
        return ResponseEntity.ok(consultationRecordService.priorHistory(patientId));
    }

    @GetMapping("/{opVisitId}/note")
    public ResponseEntity<ConsultationNoteResponse> getNote(@PathVariable Long opVisitId) {
        return ResponseEntity.ok(consultationRecordService.getNote(opVisitId));
    }

    @PutMapping("/{opVisitId}/note")
    public ResponseEntity<ConsultationNoteResponse> saveNoteDraft(@PathVariable Long opVisitId, @RequestBody ConsultationNoteRequest request) {
        return ResponseEntity.ok(consultationRecordService.saveNoteDraft(opVisitId, request));
    }

    @PostMapping("/{opVisitId}/note/complete")
    public ResponseEntity<ConsultationNoteResponse> completeNote(@PathVariable Long opVisitId) {
        return ResponseEntity.ok(consultationRecordService.completeNote(opVisitId));
    }

    @GetMapping("/{opVisitId}/prescription")
    public ResponseEntity<PrescriptionResponse> getPrescription(@PathVariable Long opVisitId) {
        return ResponseEntity.ok(consultationRecordService.getPrescription(opVisitId));
    }

    @PutMapping("/{opVisitId}/prescription")
    public ResponseEntity<PrescriptionResponse> savePrescriptionDraft(@PathVariable Long opVisitId, @Valid @RequestBody PrescriptionRequest request) {
        return ResponseEntity.ok(consultationRecordService.savePrescriptionDraft(opVisitId, request));
    }

    @PostMapping("/{opVisitId}/prescription/complete")
    public ResponseEntity<PrescriptionResponse> completePrescription(@PathVariable Long opVisitId) {
        return ResponseEntity.ok(consultationRecordService.completePrescription(opVisitId));
    }

    @GetMapping("/{opVisitId}/medicine-lookup")
    public ResponseEntity<MedicineLookupResponse> lookupMedicine(@PathVariable Long opVisitId, @RequestParam(required = false) String search) {
        return ResponseEntity.ok(consultationRecordService.lookupMedicine(opVisitId, search));
    }

    /** The printable OP card (§7.4/§9) — callable any time, watermarked "DRAFT" client-side until both the note and prescription are completed. */
    @GetMapping("/{opVisitId}/op-card")
    public ResponseEntity<OpCardResponse> opCard(@PathVariable Long opVisitId) {
        return ResponseEntity.ok(consultationRecordService.opCard(opVisitId));
    }

    /** "Cancel Consultation" (§5 principle 4) — always Pending Approval; lands in reception's existing cancellations queue. */
    @PostMapping("/{opVisitId}/cancel")
    public ResponseEntity<CancellationResponse> cancelVisit(@PathVariable Long opVisitId, @RequestBody CancelVisitRequest request) {
        return ResponseEntity.ok(consultationRecordService.cancelVisit(opVisitId, request.getReason()));
    }
}
