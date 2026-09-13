package com.lockdoc.outpatient.controller;

import com.lockdoc.outpatient.dto.*;
import com.lockdoc.outpatient.service.BillingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Master Spec §7.5 - Due Collection screen (#8), billing (#7). */
@RestController
@RequestMapping("/op/bills")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('OP_BILLING_MANAGE')")
    public ResponseEntity<BillResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(billingService.get(id));
    }

    @GetMapping("/by-encounter")
    @PreAuthorize("hasAuthority('OP_BILLING_MANAGE')")
    public ResponseEntity<BillResponse> getByEncounter(@RequestParam String encounterType, @RequestParam Long encounterId) {
        return ResponseEntity.ok(billingService.getByEncounter(encounterType.toUpperCase(), encounterId));
    }

    /** Due Collection screen (§17.7 #8) - finds a balance by patient/UHID/bill-number/mobile; omit search to list every outstanding bill. */
    @GetMapping("/due")
    @PreAuthorize("hasAuthority('OP_BILLING_MANAGE')")
    public ResponseEntity<List<BillResponse>> searchDue(@RequestParam(required = false) String search) {
        return ResponseEntity.ok(billingService.searchDue(search));
    }

    /** Every bill for one patient (Patient Record, §17.7 #3), settled or not - /due is only what's outstanding. */
    @GetMapping("/by-patient")
    @PreAuthorize("hasAuthority('OP_BILLING_MANAGE')")
    public ResponseEntity<List<BillResponse>> listForPatient(@RequestParam Long patientId) {
        return ResponseEntity.ok(billingService.listForPatient(patientId));
    }

    @PostMapping("/{id}/payments")
    @PreAuthorize("hasAuthority('OP_BILLING_MANAGE')")
    public ResponseEntity<PaymentResponse> recordPayment(@PathVariable Long id, @Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(billingService.recordPayment(id, request));
    }

    /** Refunds screen (§5 principle 4, §7.5) - cancelled bills with money still owed back, e.g. a cancelled consultation's paid fee. */
    @GetMapping("/refunds/pending")
    @PreAuthorize("hasAuthority('OP_BILLING_MANAGE')")
    public ResponseEntity<List<BillResponse>> pendingRefunds() {
        return ResponseEntity.ok(billingService.pendingRefunds());
    }

    @PostMapping("/{id}/refund")
    @PreAuthorize("hasAuthority('OP_BILLING_MANAGE')")
    public ResponseEntity<PaymentResponse> refund(@PathVariable Long id, @Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(billingService.refund(id, request));
    }

    @GetMapping("/{id}/payments")
    @PreAuthorize("hasAuthority('OP_BILLING_MANAGE')")
    public ResponseEntity<List<PaymentResponse>> listPayments(@PathVariable Long id) {
        return ResponseEntity.ok(billingService.listPayments(id));
    }

    @PostMapping("/{id}/discounts")
    @PreAuthorize("hasAuthority('OP_DISCOUNT_REQUEST')")
    public ResponseEntity<DiscountResponse> requestDiscount(@PathVariable Long id, @Valid @RequestBody DiscountRequest request) {
        return ResponseEntity.ok(billingService.requestDiscount(id, request));
    }

    @GetMapping("/discounts/pending")
    @PreAuthorize("hasAuthority('OP_DISCOUNT_APPROVE')")
    public ResponseEntity<List<DiscountResponse>> pendingDiscounts() {
        return ResponseEntity.ok(billingService.pendingDiscounts());
    }

    @PostMapping("/discounts/{discountId}/approve")
    @PreAuthorize("hasAuthority('OP_DISCOUNT_APPROVE')")
    public ResponseEntity<DiscountResponse> approveDiscount(@PathVariable Long discountId) {
        return ResponseEntity.ok(billingService.approveDiscount(discountId));
    }

    @PostMapping("/discounts/{discountId}/reject")
    @PreAuthorize("hasAuthority('OP_DISCOUNT_APPROVE')")
    public ResponseEntity<DiscountResponse> rejectDiscount(@PathVariable Long discountId) {
        return ResponseEntity.ok(billingService.rejectDiscount(discountId));
    }
}
