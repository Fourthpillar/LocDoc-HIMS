package com.lockdoc.outpatient.controller;

import com.lockdoc.outpatient.dto.CommissionBasisRequest;
import com.lockdoc.outpatient.dto.CommissionBasisResponse;
import com.lockdoc.outpatient.service.CommissionBasisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Self-service (Master Spec §6, §16 step 8) - referrer commission terms, own facility only. */
@RestController
@RequestMapping("/op/commission-basis")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('COMMISSION_BASIS_MANAGE')")
public class CommissionBasisController {

    private final CommissionBasisService commissionBasisService;

    @GetMapping
    public ResponseEntity<List<CommissionBasisResponse>> listMine() {
        return ResponseEntity.ok(commissionBasisService.listMine());
    }

    @PostMapping
    public ResponseEntity<CommissionBasisResponse> create(@Valid @RequestBody CommissionBasisRequest request) {
        return ResponseEntity.ok(commissionBasisService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommissionBasisResponse> update(@PathVariable Long id, @Valid @RequestBody CommissionBasisRequest request) {
        return ResponseEntity.ok(commissionBasisService.update(id, request));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        commissionBasisService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
