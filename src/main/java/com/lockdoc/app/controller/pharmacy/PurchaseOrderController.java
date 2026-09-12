package com.lockdoc.app.controller.pharmacy;

import com.lockdoc.app.config.AppUserPrincipal;
import com.lockdoc.app.dto.PageResponse;
import com.lockdoc.app.dto.pharmacy.PurchaseOrderRequest;
import com.lockdoc.app.dto.pharmacy.PurchaseOrderResponse;
import com.lockdoc.app.service.pharmacy.PurchaseOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pharmacy/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('PHARMACY_PURCHASE_ORDER_CREATE', 'PHARMACY_PURCHASE_ORDER_APPROVE')")
    public ResponseEntity<PageResponse<PurchaseOrderResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(purchaseOrderService.list(page, size, search));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PHARMACY_PURCHASE_ORDER_CREATE', 'PHARMACY_PURCHASE_ORDER_APPROVE')")
    public ResponseEntity<PurchaseOrderResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseOrderService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PHARMACY_PURCHASE_ORDER_CREATE')")
    public ResponseEntity<PurchaseOrderResponse> create(@Valid @RequestBody PurchaseOrderRequest request,
                                                          @AuthenticationPrincipal AppUserPrincipal principal) {
        return ResponseEntity.ok(purchaseOrderService.create(request, principal.getUserId()));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('PHARMACY_PURCHASE_ORDER_APPROVE')")
    public ResponseEntity<PurchaseOrderResponse> approve(@PathVariable Long id,
                                                           @AuthenticationPrincipal AppUserPrincipal principal) {
        return ResponseEntity.ok(purchaseOrderService.approve(id, principal.getUserId()));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('PHARMACY_PURCHASE_ORDER_APPROVE')")
    public ResponseEntity<PurchaseOrderResponse> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseOrderService.cancel(id));
    }
}
