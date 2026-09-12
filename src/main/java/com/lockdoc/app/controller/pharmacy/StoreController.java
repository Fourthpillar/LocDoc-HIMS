package com.lockdoc.app.controller.pharmacy;

import com.lockdoc.app.dto.pharmacy.StoreRequest;
import com.lockdoc.app.dto.pharmacy.StoreResponse;
import com.lockdoc.app.service.pharmacy.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pharmacy/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    // Read is available to anyone with a pharmacy right that needs to pick a store
    // (indent/transfer/stock-count all need this list), not just PHARMACY_STORE_MANAGE.
    @GetMapping
    @PreAuthorize("hasAnyAuthority('PHARMACY_STORE_MANAGE', 'PHARMACY_INDENT_CREATE', 'PHARMACY_TRANSFER_CREATE', 'PHARMACY_STOCK_COUNT_CREATE', 'PHARMACY_INVENTORY_READ')")
    public ResponseEntity<List<StoreResponse>> listForFacility() {
        return ResponseEntity.ok(storeService.listForFacility());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PHARMACY_STORE_MANAGE')")
    public ResponseEntity<StoreResponse> create(@Valid @RequestBody StoreRequest request) {
        return ResponseEntity.ok(storeService.create(request));
    }
}
