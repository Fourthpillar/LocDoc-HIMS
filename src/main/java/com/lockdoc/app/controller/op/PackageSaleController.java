package com.lockdoc.app.controller.op;

import com.lockdoc.app.dto.op.PackageConsumeRequest;
import com.lockdoc.app.dto.op.PackageSaleRequest;
import com.lockdoc.app.dto.op.PackageSaleResponse;
import com.lockdoc.app.dto.op.PackageUtilizationResponse;
import com.lockdoc.app.service.op.PackageSaleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Package sale & utilization (Master Spec §7.5, screen #11) - sell a package, then draw down its included items across visits. */
@RestController
@RequestMapping("/op/package-sales")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('OP_BILLING_MANAGE')")
public class PackageSaleController {

    private final PackageSaleService packageSaleService;

    @PostMapping
    public ResponseEntity<PackageSaleResponse> sell(@Valid @RequestBody PackageSaleRequest request) {
        return ResponseEntity.ok(packageSaleService.sell(request));
    }

    @GetMapping
    public ResponseEntity<List<PackageSaleResponse>> listForPatient(@RequestParam Long patientId) {
        return ResponseEntity.ok(packageSaleService.listForPatient(patientId));
    }

    @PostMapping("/utilization/{utilizationId}/consume")
    public ResponseEntity<PackageUtilizationResponse> consume(@PathVariable Long utilizationId, @Valid @RequestBody PackageConsumeRequest request) {
        return ResponseEntity.ok(packageSaleService.consume(utilizationId, request));
    }
}
