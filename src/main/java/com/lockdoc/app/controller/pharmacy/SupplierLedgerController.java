package com.lockdoc.app.controller.pharmacy;

import com.lockdoc.app.dto.pharmacy.SupplierLedgerEntry;
import com.lockdoc.app.service.pharmacy.SupplierLedgerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Master Spec §17.7 screen #32. */
@RestController
@RequestMapping("/pharmacy/suppliers/{supplierId}/ledger")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('PHARMACY_REPORT_READ', 'PHARMACY_SUPPLIER_MANAGE')")
public class SupplierLedgerController {

    private final SupplierLedgerService supplierLedgerService;

    @GetMapping
    public ResponseEntity<List<SupplierLedgerEntry>> forSupplier(@PathVariable Long supplierId) {
        return ResponseEntity.ok(supplierLedgerService.forSupplier(supplierId));
    }
}
