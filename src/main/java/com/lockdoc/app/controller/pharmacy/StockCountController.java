package com.lockdoc.app.controller.pharmacy;

import com.lockdoc.app.config.AppUserPrincipal;
import com.lockdoc.app.dto.PageResponse;
import com.lockdoc.app.dto.pharmacy.StockCountRequest;
import com.lockdoc.app.dto.pharmacy.StockCountResponse;
import com.lockdoc.app.dto.pharmacy.StockCountSubmitRequest;
import com.lockdoc.app.service.pharmacy.StockCountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** Master Spec §17.7 screen #27 - restored here, was missing from every earlier revision (§11.4). */
@RestController
@RequestMapping("/pharmacy/stock-counts")
@RequiredArgsConstructor
public class StockCountController {

    private final StockCountService stockCountService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('PHARMACY_STOCK_COUNT_CREATE', 'PHARMACY_STOCK_COUNT_APPROVE')")
    public ResponseEntity<PageResponse<StockCountResponse>> list(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(stockCountService.list(page, size));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PHARMACY_STOCK_COUNT_CREATE', 'PHARMACY_STOCK_COUNT_APPROVE')")
    public ResponseEntity<StockCountResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(stockCountService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PHARMACY_STOCK_COUNT_CREATE')")
    public ResponseEntity<StockCountResponse> create(@Valid @RequestBody StockCountRequest request, @AuthenticationPrincipal AppUserPrincipal principal) {
        return ResponseEntity.ok(stockCountService.create(request, principal.getUserId()));
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAuthority('PHARMACY_STOCK_COUNT_CREATE')")
    public ResponseEntity<StockCountResponse> submit(@PathVariable Long id, @Valid @RequestBody StockCountSubmitRequest request) {
        return ResponseEntity.ok(stockCountService.submit(id, request));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('PHARMACY_STOCK_COUNT_APPROVE')")
    public ResponseEntity<StockCountResponse> approve(@PathVariable Long id, @AuthenticationPrincipal AppUserPrincipal principal) {
        return ResponseEntity.ok(stockCountService.approve(id, principal.getUserId()));
    }
}
