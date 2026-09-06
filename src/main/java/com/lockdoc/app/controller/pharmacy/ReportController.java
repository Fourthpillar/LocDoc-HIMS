package com.lockdoc.app.controller.pharmacy;

import com.lockdoc.app.dto.PageResponse;
import com.lockdoc.app.dto.pharmacy.MedicineBatchResponse;
import com.lockdoc.app.dto.pharmacy.MedicineSalesReportRow;
import com.lockdoc.app.dto.pharmacy.PurchaseOrderResponse;
import com.lockdoc.app.dto.pharmacy.PurchaseResponse;
import com.lockdoc.app.dto.pharmacy.SalesInvoiceResponse;
import com.lockdoc.app.dto.pharmacy.SalesReturnResponse;
import com.lockdoc.app.dto.pharmacy.StockDetailReportRow;
import com.lockdoc.app.dto.pharmacy.StockSummaryResponse;
import com.lockdoc.app.service.pharmacy.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/pharmacy/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/stock-summary")
    @PreAuthorize("hasAuthority('PHARMACY_REPORT_READ')")
    public ResponseEntity<PageResponse<StockSummaryResponse>> stockSummary(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(reportService.stockSummary(page, size));
    }

    @GetMapping("/expiry")
    @PreAuthorize("hasAuthority('PHARMACY_REPORT_READ')")
    public ResponseEntity<List<MedicineBatchResponse>> expiry(@RequestParam(defaultValue = "30") int withinDays) {
        return ResponseEntity.ok(reportService.expiryReport(withinDays));
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasAuthority('PHARMACY_REPORT_READ')")
    public ResponseEntity<List<StockSummaryResponse>> lowStock() {
        return ResponseEntity.ok(reportService.lowStockReport());
    }

    @GetMapping("/sales-register")
    @PreAuthorize("hasAuthority('PHARMACY_REPORT_READ')")
    public ResponseEntity<List<SalesInvoiceResponse>> salesRegister(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(reportService.salesRegister(from, to));
    }

    @GetMapping("/purchase-register")
    @PreAuthorize("hasAuthority('PHARMACY_REPORT_READ')")
    public ResponseEntity<List<PurchaseResponse>> purchaseRegister(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(reportService.purchaseRegister(from, to));
    }

    @GetMapping("/sales-return-register")
    @PreAuthorize("hasAuthority('PHARMACY_REPORT_READ')")
    public ResponseEntity<List<SalesReturnResponse>> salesReturnRegister(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(reportService.salesReturnRegister(from, to));
    }

    @GetMapping("/purchase-orders")
    @PreAuthorize("hasAuthority('PHARMACY_REPORT_READ')")
    public ResponseEntity<List<PurchaseOrderResponse>> purchaseOrderReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(reportService.purchaseOrderReport(from, to));
    }

    @GetMapping("/purchase-dues")
    @PreAuthorize("hasAuthority('PHARMACY_REPORT_READ')")
    public ResponseEntity<List<PurchaseResponse>> purchaseDues() {
        return ResponseEntity.ok(reportService.purchaseDuesReport());
    }

    @GetMapping("/medicine-sales")
    @PreAuthorize("hasAuthority('PHARMACY_REPORT_READ')")
    public ResponseEntity<List<MedicineSalesReportRow>> medicineSales(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(reportService.medicineSalesReport(from, to));
    }

    @GetMapping("/stock-detail")
    @PreAuthorize("hasAuthority('PHARMACY_REPORT_READ')")
    public ResponseEntity<List<StockDetailReportRow>> stockDetail() {
        return ResponseEntity.ok(reportService.stockDetailReport());
    }
}
