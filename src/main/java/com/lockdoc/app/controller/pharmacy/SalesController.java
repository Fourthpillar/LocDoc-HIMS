package com.lockdoc.app.controller.pharmacy;

import com.lockdoc.app.dto.PageResponse;
import com.lockdoc.app.dto.pharmacy.SalesInvoiceRequest;
import com.lockdoc.app.dto.pharmacy.SalesInvoiceResponse;
import com.lockdoc.app.entity.User;
import com.lockdoc.app.repository.UserRepository;
import com.lockdoc.app.service.pharmacy.SalesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pharmacy/sales")
@RequiredArgsConstructor
public class SalesController {

    private final SalesService salesService;
    private final UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('PHARMACY_SALE_CREATE')")
    public ResponseEntity<PageResponse<SalesInvoiceResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(salesService.list(page, size, search));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PHARMACY_SALE_CREATE')")
    public ResponseEntity<SalesInvoiceResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(salesService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PHARMACY_SALE_CREATE')")
    public ResponseEntity<SalesInvoiceResponse> create(@Valid @RequestBody SalesInvoiceRequest request,
                                                         @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(salesService.create(request, currentUserId(userDetails)));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('PHARMACY_SALE_CREATE')")
    public ResponseEntity<SalesInvoiceResponse> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(salesService.cancel(id));
    }

    private Long currentUserId(UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        return user.getId();
    }
}
