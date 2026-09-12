package com.lockdoc.app.controller.pharmacy;

import com.lockdoc.app.config.SecurityUtils;
import com.lockdoc.app.dto.pharmacy.StatutoryRegisterEntryResponse;
import com.lockdoc.app.repository.pharmacy.StatutoryRegisterEntryRepository;
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

/**
 * Statutory register viewer/export (Master Spec §17.7 screen #30, §15.2)
 * - the entity behind "build at launch" for the drug registers, finally
 * viewable. Export itself is a frontend concern (the same filtered rows
 * this endpoint already returns, rendered to PDF/CSV client-side) - no
 * separate export endpoint needed.
 */
@RestController
@RequestMapping("/pharmacy/statutory-register")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('PHARMACY_REPORT_READ')")
public class StatutoryRegisterController {

    private final StatutoryRegisterEntryRepository statutoryRegisterEntryRepository;

    @GetMapping
    public ResponseEntity<List<StatutoryRegisterEntryResponse>> list(
            @RequestParam String registerType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        Long facilityId = SecurityUtils.requireFacilityId();
        List<StatutoryRegisterEntryResponse> entries = statutoryRegisterEntryRepository
                .findByFacilityIdAndRegisterTypeAndSaleDateBetweenOrderBySaleDateAsc(facilityId, registerType, from, to)
                .stream().map(StatutoryRegisterEntryResponse::toResponse).toList();
        return ResponseEntity.ok(entries);
    }
}
