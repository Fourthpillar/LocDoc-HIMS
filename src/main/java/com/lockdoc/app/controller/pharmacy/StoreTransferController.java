package com.lockdoc.app.controller.pharmacy;

import com.lockdoc.app.config.AppUserPrincipal;
import com.lockdoc.app.dto.PageResponse;
import com.lockdoc.app.dto.pharmacy.StoreTransferReceiveRequest;
import com.lockdoc.app.dto.pharmacy.StoreTransferRequest;
import com.lockdoc.app.dto.pharmacy.StoreTransferResponse;
import com.lockdoc.app.service.pharmacy.StoreTransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** Master Spec §17.7 screen #26 - restored here, was missing from every earlier revision (§11.4). */
@RestController
@RequestMapping("/pharmacy/transfers")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('PHARMACY_TRANSFER_CREATE')")
public class StoreTransferController {

    private final StoreTransferService storeTransferService;

    @GetMapping
    public ResponseEntity<PageResponse<StoreTransferResponse>> list(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(storeTransferService.list(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StoreTransferResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(storeTransferService.get(id));
    }

    @PostMapping
    public ResponseEntity<StoreTransferResponse> create(@Valid @RequestBody StoreTransferRequest request, @AuthenticationPrincipal AppUserPrincipal principal) {
        return ResponseEntity.ok(storeTransferService.create(request, principal.getUserId()));
    }

    @PostMapping("/{id}/receive")
    public ResponseEntity<StoreTransferResponse> receive(@PathVariable Long id, @Valid @RequestBody StoreTransferReceiveRequest request,
                                                           @AuthenticationPrincipal AppUserPrincipal principal) {
        return ResponseEntity.ok(storeTransferService.receive(id, request, principal.getUserId()));
    }
}
