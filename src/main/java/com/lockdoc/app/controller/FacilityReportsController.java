package com.lockdoc.app.controller;

import com.lockdoc.app.dto.PageResponse;
import com.lockdoc.app.dto.op.MlcRegisterResponse;
import com.lockdoc.app.dto.platform.AuditLogResponse;
import com.lockdoc.app.service.FacilityReportsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/** Facility reports & audit log, incl. MLC register (Master Spec §17.7, screen #38) - Hospital/Clinic Admin, own facility only. */
@RestController
@RequestMapping("/facility/reports")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('FACILITY_REPORTS_VIEW')")
public class FacilityReportsController {

    private final FacilityReportsService facilityReportsService;

    @GetMapping("/mlc-register")
    public ResponseEntity<MlcRegisterResponse> mlcRegister(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(facilityReportsService.mlcRegister(from, to));
    }

    @GetMapping("/audit-log")
    public ResponseEntity<PageResponse<AuditLogResponse>> auditLog(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(facilityReportsService.auditLog(page, size));
    }
}
