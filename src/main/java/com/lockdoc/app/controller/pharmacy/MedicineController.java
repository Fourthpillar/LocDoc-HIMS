package com.lockdoc.app.controller.pharmacy;

import com.lockdoc.app.dto.PageResponse;
import com.lockdoc.app.dto.pharmacy.MedicineRequest;
import com.lockdoc.app.dto.pharmacy.MedicineResponse;
import com.lockdoc.app.service.pharmacy.MedicineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pharmacy/medicines")
@RequiredArgsConstructor
public class MedicineController {

    private final MedicineService medicineService;

    @GetMapping
    @PreAuthorize("hasAuthority('PHARMACY_INVENTORY_READ')")
    public ResponseEntity<PageResponse<MedicineResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(medicineService.list(page, size, search));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PHARMACY_INVENTORY_READ')")
    public ResponseEntity<MedicineResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(medicineService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PHARMACY_MEDICINE_MANAGE')")
    public ResponseEntity<MedicineResponse> create(@Valid @RequestBody MedicineRequest request) {
        return ResponseEntity.ok(medicineService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PHARMACY_MEDICINE_MANAGE')")
    public ResponseEntity<MedicineResponse> update(@PathVariable Long id, @Valid @RequestBody MedicineRequest request) {
        return ResponseEntity.ok(medicineService.update(id, request));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('PHARMACY_MEDICINE_MANAGE')")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        medicineService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
