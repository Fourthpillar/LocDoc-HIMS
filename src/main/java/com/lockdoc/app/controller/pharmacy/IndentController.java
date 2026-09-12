package com.lockdoc.app.controller.pharmacy;

import com.lockdoc.app.config.AppUserPrincipal;
import com.lockdoc.app.dto.PageResponse;
import com.lockdoc.app.dto.pharmacy.IndentLineQtyListRequest;
import com.lockdoc.app.dto.pharmacy.IndentRequest;
import com.lockdoc.app.dto.pharmacy.IndentResponse;
import com.lockdoc.app.service.pharmacy.IndentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** Master Spec §17.7 screen #21 - restored here, was missing from every earlier revision (§11.3). */
@RestController
@RequestMapping("/pharmacy/indents")
@RequiredArgsConstructor
public class IndentController {

    private final IndentService indentService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('PHARMACY_INDENT_CREATE', 'PHARMACY_INDENT_APPROVE')")
    public ResponseEntity<PageResponse<IndentResponse>> list(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(indentService.list(page, size, status));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PHARMACY_INDENT_CREATE', 'PHARMACY_INDENT_APPROVE')")
    public ResponseEntity<IndentResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(indentService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PHARMACY_INDENT_CREATE')")
    public ResponseEntity<IndentResponse> create(@Valid @RequestBody IndentRequest request, @AuthenticationPrincipal AppUserPrincipal principal) {
        return ResponseEntity.ok(indentService.create(request, principal.getUserId()));
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAuthority('PHARMACY_INDENT_CREATE')")
    public ResponseEntity<IndentResponse> submit(@PathVariable Long id) {
        return ResponseEntity.ok(indentService.submit(id));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('PHARMACY_INDENT_APPROVE')")
    public ResponseEntity<IndentResponse> approve(@PathVariable Long id, @Valid @RequestBody IndentLineQtyListRequest request,
                                                   @AuthenticationPrincipal AppUserPrincipal principal) {
        return ResponseEntity.ok(indentService.approve(id, request, principal.getUserId()));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('PHARMACY_INDENT_APPROVE')")
    public ResponseEntity<IndentResponse> reject(@PathVariable Long id) {
        return ResponseEntity.ok(indentService.reject(id));
    }

    @PostMapping("/{id}/issue")
    @PreAuthorize("hasAuthority('PHARMACY_INDENT_CREATE')")
    public ResponseEntity<IndentResponse> issue(@PathVariable Long id, @Valid @RequestBody IndentLineQtyListRequest request,
                                                 @AuthenticationPrincipal AppUserPrincipal principal) {
        return ResponseEntity.ok(indentService.issue(id, request, principal.getUserId()));
    }
}
