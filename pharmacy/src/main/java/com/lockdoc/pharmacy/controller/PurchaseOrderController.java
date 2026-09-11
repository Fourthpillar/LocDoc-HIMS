package com.lockdoc.pharmacy.controller;

import com.lockdoc.common.dto.PageResponse;
import com.lockdoc.pharmacy.dto.PurchaseOrderRequest;
import com.lockdoc.pharmacy.dto.PurchaseOrderResponse;
import com.lockdoc.common.entity.User;
import com.lockdoc.common.repository.UserRepository;
import com.lockdoc.pharmacy.service.PurchaseOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pharmacy/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;
    private final UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('PHARMACY_PURCHASE_ORDER_CREATE')")
    public ResponseEntity<PageResponse<PurchaseOrderResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(purchaseOrderService.list(page, size, search));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PHARMACY_PURCHASE_ORDER_CREATE')")
    public ResponseEntity<PurchaseOrderResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseOrderService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PHARMACY_PURCHASE_ORDER_CREATE')")
    public ResponseEntity<PurchaseOrderResponse> create(@Valid @RequestBody PurchaseOrderRequest request,
                                                          @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(purchaseOrderService.create(request, currentUserId(userDetails)));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('PHARMACY_PURCHASE_ORDER_APPROVE')")
    public ResponseEntity<PurchaseOrderResponse> approve(@PathVariable Long id,
                                                           @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(purchaseOrderService.approve(id, currentUserId(userDetails)));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('PHARMACY_PURCHASE_ORDER_APPROVE')")
    public ResponseEntity<PurchaseOrderResponse> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseOrderService.cancel(id));
    }

    private Long currentUserId(UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        return user.getId();
    }
}
