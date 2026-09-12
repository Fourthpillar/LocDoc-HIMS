package com.lockdoc.app.controller.op;

import com.lockdoc.app.dto.doctor.DoctorStatedHoursResponse;
import com.lockdoc.app.service.doctor.DoctorFacilityMappingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * The front desk's read of when each doctor says they consult here (§8.2).
 *
 * Booking rights are enough: whoever is putting patients into a doctor's day
 * is exactly who needs to know the doctor only comes in on Tuesday mornings.
 */
@RestController
@RequestMapping("/op/doctor-consultation-hours")
@RequiredArgsConstructor
public class DoctorConsultationHoursController {

    private final DoctorFacilityMappingService mappingService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('APPOINTMENT_MANAGE', 'DOCTOR_SCHEDULE_MANAGE')")
    public ResponseEntity<List<DoctorStatedHoursResponse>> listForFacility() {
        return ResponseEntity.ok(mappingService.statedHoursForMyFacility());
    }
}
