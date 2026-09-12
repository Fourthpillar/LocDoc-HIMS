package com.lockdoc.app.controller.op;

import com.lockdoc.app.dto.op.*;
import com.lockdoc.app.service.op.OpReportsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * OP Reports (Master Spec §17.7 #12a) - Receptionist and Hospital/Clinic
 * Admin, per spec's own access list; both already hold OP_BILLING_MANAGE
 * so no new right. See OpReportsService's own javadoc for which three of
 * the eight named reports aren't built yet and why.
 */
@RestController
@RequestMapping("/op/reports")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('OP_BILLING_MANAGE')")
public class OpReportsController {

    private final OpReportsService reportsService;

    @GetMapping("/registrations")
    public ResponseEntity<RegistrationsReportResponse> registrations(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(reportsService.registrations(from, to));
    }

    @GetMapping("/day-collection")
    public ResponseEntity<DayCollectionReportResponse> dayCollection(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(reportsService.dayCollection(from, to));
    }

    @GetMapping("/discounts")
    public ResponseEntity<DiscountsReportResponse> discounts(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(reportsService.discounts(from, to));
    }

    @GetMapping("/cancellations")
    public ResponseEntity<CancellationsReportResponse> cancellations(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(reportsService.cancellations(from, to));
    }

    @GetMapping("/pharmacy-conversion")
    public ResponseEntity<PharmacyConversionReportResponse> pharmacyConversion(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(reportsService.pharmacyConversion(from, to));
    }

    @GetMapping("/free-reviews")
    public ResponseEntity<FreeReviewsReportResponse> freeReviews(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(reportsService.freeReviews(from, to));
    }

    @GetMapping("/commission")
    public ResponseEntity<CommissionReportResponse> commission(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(reportsService.commission(from, to));
    }

    @GetMapping("/area-wise-consultations")
    public ResponseEntity<AreaWiseConsultationsReportResponse> areaWiseConsultations(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(reportsService.areaWiseConsultations(from, to));
    }
}
