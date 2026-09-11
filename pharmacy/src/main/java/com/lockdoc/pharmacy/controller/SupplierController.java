package com.lockdoc.pharmacy.controller;

import com.lockdoc.common.dto.PageResponse;
import com.lockdoc.pharmacy.dto.SupplierRequest;
import com.lockdoc.pharmacy.dto.SupplierResponse;
import com.lockdoc.pharmacy.service.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pharmacy/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @GetMapping
    @PreAuthorize("hasAuthority('PHARMACY_SUPPLIER_MANAGE')")
    public ResponseEntity<PageResponse<SupplierResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(supplierService.list(page, size, search));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PHARMACY_SUPPLIER_MANAGE')")
    public ResponseEntity<SupplierResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(supplierService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PHARMACY_SUPPLIER_MANAGE')")
    public ResponseEntity<SupplierResponse> create(@Valid @RequestBody SupplierRequest request) {
        return ResponseEntity.ok(supplierService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PHARMACY_SUPPLIER_MANAGE')")
    public ResponseEntity<SupplierResponse> update(@PathVariable Long id, @Valid @RequestBody SupplierRequest request) {
        return ResponseEntity.ok(supplierService.update(id, request));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('PHARMACY_SUPPLIER_MANAGE')")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        supplierService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
