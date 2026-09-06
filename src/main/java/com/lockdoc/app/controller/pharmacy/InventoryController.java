package com.lockdoc.app.controller.pharmacy;

import com.lockdoc.app.dto.PageResponse;
import com.lockdoc.app.dto.pharmacy.MedicineBatchResponse;
import com.lockdoc.app.dto.pharmacy.StockSummaryResponse;
import com.lockdoc.app.service.pharmacy.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/pharmacy/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    @PreAuthorize("hasAuthority('PHARMACY_INVENTORY_READ')")
    public ResponseEntity<PageResponse<StockSummaryResponse>> getStockSummary(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "false") boolean lowStockOnly) {
        return ResponseEntity.ok(inventoryService.getStockSummary(page, size, lowStockOnly));
    }

    @GetMapping("/{medicineId}/batches")
    @PreAuthorize("hasAuthority('PHARMACY_INVENTORY_READ')")
    public ResponseEntity<List<MedicineBatchResponse>> getBatches(@PathVariable Long medicineId) {
        return ResponseEntity.ok(inventoryService.getBatches(medicineId));
    }
}
