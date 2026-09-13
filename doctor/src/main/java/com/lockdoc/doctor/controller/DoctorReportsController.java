package com.lockdoc.doctor.controller;

import com.lockdoc.doctor.dto.ConsultationCountReportResponse;
import com.lockdoc.doctor.dto.MedicinesPrescribedReportResponse;
import com.lockdoc.doctor.dto.PunctualityReportResponse;
import com.lockdoc.doctor.service.DoctorReportsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/** Doctor Reports (Master Spec §17.7, screen #18) - own data only. */
@RestController
@RequestMapping("/doctor/reports")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('DOCTOR_REPORTS_VIEW')")
public class DoctorReportsController {

    private final DoctorReportsService reportsService;

    @GetMapping("/punctuality")
    public ResponseEntity<PunctualityReportResponse> punctuality(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Long facilityId) {
        return ResponseEntity.ok(reportsService.punctuality(from, to, facilityId));
    }

    @GetMapping("/consultation-count")
    public ResponseEntity<ConsultationCountReportResponse> consultationCount(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Long facilityId) {
        return ResponseEntity.ok(reportsService.consultationCount(from, to, facilityId));
    }

    @GetMapping("/medicines-prescribed")
    public ResponseEntity<MedicinesPrescribedReportResponse> medicinesPrescribed(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Long facilityId) {
        return ResponseEntity.ok(reportsService.medicinesPrescribed(from, to, facilityId));
    }
}
