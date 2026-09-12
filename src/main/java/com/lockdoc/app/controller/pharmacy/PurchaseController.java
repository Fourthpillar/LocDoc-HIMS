package com.lockdoc.app.controller.pharmacy;

import com.lockdoc.app.config.AppUserPrincipal;
import com.lockdoc.app.dto.PageResponse;
import com.lockdoc.app.dto.pharmacy.PurchaseRequest;
import com.lockdoc.app.dto.pharmacy.PurchaseResponse;
import com.lockdoc.app.service.pharmacy.PurchaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pharmacy/purchases")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;

    @GetMapping
    @PreAuthorize("hasAuthority('PHARMACY_PURCHASE_CREATE')")
    public ResponseEntity<PageResponse<PurchaseResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(purchaseService.list(page, size, search));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PHARMACY_PURCHASE_CREATE')")
    public ResponseEntity<PurchaseResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PHARMACY_PURCHASE_CREATE')")
    public ResponseEntity<PurchaseResponse> create(@Valid @RequestBody PurchaseRequest request,
                                                     @AuthenticationPrincipal AppUserPrincipal principal) {
        return ResponseEntity.ok(purchaseService.create(request, principal.getUserId()));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('PHARMACY_PURCHASE_CREATE')")
    public ResponseEntity<PurchaseResponse> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseService.cancel(id));
    }
}
