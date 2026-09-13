package com.lockdoc.app.controller.op;

import com.lockdoc.app.dto.op.*;
import com.lockdoc.app.service.op.BillableItemService;
import com.lockdoc.app.service.op.MastersService;
import com.lockdoc.app.service.op.PackageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Masters (Master Spec §7.2, screen #34) - referral doctors, PRO,
 * Organization/TPA, billable items (procedure/service charges), and
 * packages, own facility only. Billable items and packages each sit
 * under their own right (PROCEDURE_MASTERS_MANAGE, PACKAGE_MASTERS_MANAGE)
 * rather than FACILITY_MASTERS_MANAGE - method-level @PreAuthorize below,
 * not a class-level one, since the rights differ.
 */
@RestController
@RequestMapping("/op/masters")
@RequiredArgsConstructor
public class MastersController {

    private final MastersService mastersService;
    private final BillableItemService billableItemService;
    private final PackageService packageService;

    /*
     * Reading these two is not the same decision as editing them. Reception has to attribute
     * a visit to the doctor or PRO who referred it - that is what drives the commission
     * report (7.5) - but only an admin curates the list. Gated on FACILITY_MASTERS_MANAGE
     * alone, reception silently received an empty list and the referral fields never
     * appeared, so no visit booked at the desk could ever carry a referral.
     */
    @GetMapping("/referral-doctors")
    @PreAuthorize("hasAnyAuthority('FACILITY_MASTERS_MANAGE', 'OP_VISIT_MANAGE')")
    public ResponseEntity<List<ReferralDoctorResponse>> listReferralDoctors() {
        return ResponseEntity.ok(mastersService.listReferralDoctors());
    }

    @PostMapping("/referral-doctors")
    @PreAuthorize("hasAuthority('FACILITY_MASTERS_MANAGE')")
    public ResponseEntity<ReferralDoctorResponse> createReferralDoctor(@Valid @RequestBody ReferralDoctorRequest request) {
        return ResponseEntity.ok(mastersService.createReferralDoctor(request));
    }

    @PutMapping("/referral-doctors/{id}")
    @PreAuthorize("hasAuthority('FACILITY_MASTERS_MANAGE')")
    public ResponseEntity<ReferralDoctorResponse> updateReferralDoctor(@PathVariable Long id, @Valid @RequestBody ReferralDoctorRequest request) {
        return ResponseEntity.ok(mastersService.updateReferralDoctor(id, request));
    }

    @PatchMapping("/referral-doctors/{id}/deactivate")
    @PreAuthorize("hasAuthority('FACILITY_MASTERS_MANAGE')")
    public ResponseEntity<Void> deactivateReferralDoctor(@PathVariable Long id) {
        mastersService.deactivateReferralDoctor(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/pros")
    @PreAuthorize("hasAnyAuthority('FACILITY_MASTERS_MANAGE', 'OP_VISIT_MANAGE')")
    public ResponseEntity<List<ProResponse>> listPros() {
        return ResponseEntity.ok(mastersService.listPros());
    }

    @PostMapping("/pros")
    @PreAuthorize("hasAuthority('FACILITY_MASTERS_MANAGE')")
    public ResponseEntity<ProResponse> createPro(@Valid @RequestBody ProRequest request) {
        return ResponseEntity.ok(mastersService.createPro(request));
    }

    @PutMapping("/pros/{id}")
    @PreAuthorize("hasAuthority('FACILITY_MASTERS_MANAGE')")
    public ResponseEntity<ProResponse> updatePro(@PathVariable Long id, @Valid @RequestBody ProRequest request) {
        return ResponseEntity.ok(mastersService.updatePro(id, request));
    }

    @PatchMapping("/pros/{id}/deactivate")
    @PreAuthorize("hasAuthority('FACILITY_MASTERS_MANAGE')")
    public ResponseEntity<Void> deactivatePro(@PathVariable Long id) {
        mastersService.deactivatePro(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/organizations")
    @PreAuthorize("hasAuthority('FACILITY_MASTERS_MANAGE')")
    public ResponseEntity<List<OrganizationResponse>> listOrganizations() {
        return ResponseEntity.ok(mastersService.listOrganizations());
    }

    @PostMapping("/organizations")
    @PreAuthorize("hasAuthority('FACILITY_MASTERS_MANAGE')")
    public ResponseEntity<OrganizationResponse> createOrganization(@Valid @RequestBody OrganizationRequest request) {
        return ResponseEntity.ok(mastersService.createOrganization(request));
    }

    @PutMapping("/organizations/{id}")
    @PreAuthorize("hasAuthority('FACILITY_MASTERS_MANAGE')")
    public ResponseEntity<OrganizationResponse> updateOrganization(@PathVariable Long id, @Valid @RequestBody OrganizationRequest request) {
        return ResponseEntity.ok(mastersService.updateOrganization(id, request));
    }

    @PatchMapping("/organizations/{id}/deactivate")
    @PreAuthorize("hasAuthority('FACILITY_MASTERS_MANAGE')")
    public ResponseEntity<Void> deactivateOrganization(@PathVariable Long id) {
        mastersService.deactivateOrganization(id);
        return ResponseEntity.noContent().build();
    }

    // ---- Billable items (procedure/service charges, §7.2) ----

    @GetMapping("/billable-items")
    @PreAuthorize("hasAnyAuthority('PROCEDURE_MASTERS_MANAGE', 'OP_BILLING_MANAGE')")
    public ResponseEntity<List<BillableItemResponse>> listBillableItems(@RequestParam String itemType) {
        return ResponseEntity.ok(billableItemService.list(itemType));
    }

    @PostMapping("/billable-items")
    @PreAuthorize("hasAuthority('PROCEDURE_MASTERS_MANAGE')")
    public ResponseEntity<BillableItemResponse> createBillableItem(@Valid @RequestBody BillableItemRequest request) {
        return ResponseEntity.ok(billableItemService.create(request));
    }

    @PutMapping("/billable-items/{id}")
    @PreAuthorize("hasAuthority('PROCEDURE_MASTERS_MANAGE')")
    public ResponseEntity<BillableItemResponse> updateBillableItem(@PathVariable Long id, @Valid @RequestBody BillableItemRequest request) {
        return ResponseEntity.ok(billableItemService.update(id, request));
    }

    @PatchMapping("/billable-items/{id}/deactivate")
    @PreAuthorize("hasAuthority('PROCEDURE_MASTERS_MANAGE')")
    public ResponseEntity<Void> deactivateBillableItem(@PathVariable Long id) {
        billableItemService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    // ---- Packages (§6/§7.5, screen #11) ----

    @GetMapping("/packages")
    @PreAuthorize("hasAnyAuthority('PACKAGE_MASTERS_MANAGE', 'OP_BILLING_MANAGE')")
    public ResponseEntity<List<PackageResponse>> listPackages() {
        return ResponseEntity.ok(packageService.list());
    }

    @PostMapping("/packages")
    @PreAuthorize("hasAuthority('PACKAGE_MASTERS_MANAGE')")
    public ResponseEntity<PackageResponse> createPackage(@Valid @RequestBody PackageRequest request) {
        return ResponseEntity.ok(packageService.create(request));
    }

    @PutMapping("/packages/{id}")
    @PreAuthorize("hasAuthority('PACKAGE_MASTERS_MANAGE')")
    public ResponseEntity<PackageResponse> updatePackage(@PathVariable Long id, @Valid @RequestBody PackageRequest request) {
        return ResponseEntity.ok(packageService.update(id, request));
    }

    @PatchMapping("/packages/{id}/deactivate")
    @PreAuthorize("hasAuthority('PACKAGE_MASTERS_MANAGE')")
    public ResponseEntity<Void> deactivatePackage(@PathVariable Long id) {
        packageService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    // ---- Area (Country/State/City/Area, §7.2, V47) ----

    @GetMapping("/areas")
    @PreAuthorize("hasAnyAuthority('AREA_MASTERS_MANAGE', 'PATIENT_MANAGE')")
    public ResponseEntity<List<AreaResponse>> listAreas() {
        return ResponseEntity.ok(mastersService.listAreas());
    }

    @PostMapping("/areas")
    @PreAuthorize("hasAuthority('AREA_MASTERS_MANAGE')")
    public ResponseEntity<AreaResponse> createArea(@Valid @RequestBody AreaRequest request) {
        return ResponseEntity.ok(mastersService.createArea(request));
    }

    @PutMapping("/areas/{id}")
    @PreAuthorize("hasAuthority('AREA_MASTERS_MANAGE')")
    public ResponseEntity<AreaResponse> updateArea(@PathVariable Long id, @Valid @RequestBody AreaRequest request) {
        return ResponseEntity.ok(mastersService.updateArea(id, request));
    }

    @PatchMapping("/areas/{id}/deactivate")
    @PreAuthorize("hasAuthority('AREA_MASTERS_MANAGE')")
    public ResponseEntity<Void> deactivateArea(@PathVariable Long id) {
        mastersService.deactivateArea(id);
        return ResponseEntity.noContent().build();
    }
}
