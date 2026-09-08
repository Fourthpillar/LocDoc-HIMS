package com.lockdoc.app.controller.pharmacy;

import com.lockdoc.app.dto.PageResponse;
import com.lockdoc.app.dto.pharmacy.SalesReturnRequest;
import com.lockdoc.app.dto.pharmacy.SalesReturnResponse;
import com.lockdoc.app.entity.User;
import com.lockdoc.app.repository.UserRepository;
import com.lockdoc.app.service.pharmacy.SalesReturnService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pharmacy/sales-returns")
@RequiredArgsConstructor
public class SalesReturnController {

    private final SalesReturnService salesReturnService;
    private final UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('PHARMACY_SALE_RETURN_CREATE')")
    public ResponseEntity<PageResponse<SalesReturnResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(salesReturnService.list(page, size, search));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PHARMACY_SALE_RETURN_CREATE')")
    public ResponseEntity<SalesReturnResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(salesReturnService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PHARMACY_SALE_RETURN_CREATE')")
    public ResponseEntity<SalesReturnResponse> create(@Valid @RequestBody SalesReturnRequest request,
                                                        @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(salesReturnService.create(request, currentUserId(userDetails)));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('PHARMACY_SALE_RETURN_CREATE')")
    public ResponseEntity<SalesReturnResponse> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(salesReturnService.cancel(id));
    }

    private Long currentUserId(UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        return user.getId();
    }
}
