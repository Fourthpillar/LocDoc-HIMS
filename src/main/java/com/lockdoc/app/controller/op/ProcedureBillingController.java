package com.lockdoc.app.controller.op;

import com.lockdoc.app.dto.op.ProcedureBillRequest;
import com.lockdoc.app.dto.op.ProcedureBillResponse;
import com.lockdoc.app.service.op.ProcedureBillingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Procedure billing (Master Spec §7.5) - the third billable OP encounter alongside registration/consultation. */
@RestController
@RequestMapping("/op/procedure-bills")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('OP_BILLING_MANAGE')")
public class ProcedureBillingController {

    private final ProcedureBillingService procedureBillingService;

    @PostMapping
    public ResponseEntity<ProcedureBillResponse> bill(@Valid @RequestBody ProcedureBillRequest request) {
        return ResponseEntity.ok(procedureBillingService.bill(request));
    }

    @GetMapping
    public ResponseEntity<List<ProcedureBillResponse>> listForPatient(@RequestParam Long patientId) {
        return ResponseEntity.ok(procedureBillingService.listForPatient(patientId));
    }
}
