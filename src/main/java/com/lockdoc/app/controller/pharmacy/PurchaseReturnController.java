package com.lockdoc.app.controller.pharmacy;

import com.lockdoc.app.config.AppUserPrincipal;
import com.lockdoc.app.dto.PageResponse;
import com.lockdoc.app.dto.pharmacy.PurchaseReturnRequest;
import com.lockdoc.app.dto.pharmacy.PurchaseReturnResponse;
import com.lockdoc.app.service.pharmacy.PurchaseReturnService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** Master Spec §17.7 screen #25 - restored here, was missing from every earlier revision (§11.3). */
@RestController
@RequestMapping("/pharmacy/purchase-returns")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('PHARMACY_PURCHASE_RETURN_CREATE')")
public class PurchaseReturnController {

    private final PurchaseReturnService purchaseReturnService;

    @GetMapping
    public ResponseEntity<PageResponse<PurchaseReturnResponse>> list(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(purchaseReturnService.list(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseReturnResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseReturnService.get(id));
    }

    @PostMapping
    public ResponseEntity<PurchaseReturnResponse> create(@Valid @RequestBody PurchaseReturnRequest request, @AuthenticationPrincipal AppUserPrincipal principal) {
        return ResponseEntity.ok(purchaseReturnService.create(request, principal.getUserId()));
    }
}
