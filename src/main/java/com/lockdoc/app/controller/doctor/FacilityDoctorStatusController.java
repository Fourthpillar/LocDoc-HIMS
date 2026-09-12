package com.lockdoc.app.controller.doctor;

import com.lockdoc.app.dto.doctor.DoctorStatusOverrideRequest;
import com.lockdoc.app.dto.doctor.DoctorStatusResponse;
import com.lockdoc.app.service.doctor.DoctorStatusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Hospital/Clinic Admin's (and, via the same right, Receptionist's) view
 * of doctor status at their own facility (Master Spec §8.3), plus the
 * status-override control (build order step 5 — attached to the
 * front-desk day list, per step 4's own fix note on why it wasn't built
 * there).
 */
@RestController
@RequestMapping("/facility/doctor-status")
@RequiredArgsConstructor
public class FacilityDoctorStatusController {

    private final DoctorStatusService statusService;

    @GetMapping("/today")
    @PreAuthorize("hasAuthority('FACILITY_DOCTOR_STATUS_VIEW')")
    public ResponseEntity<List<DoctorStatusResponse>> facilityStatusToday() {
        return ResponseEntity.ok(statusService.facilityStatusToday());
    }

    @PostMapping("/override")
    @PreAuthorize("hasAuthority('RECEPTION_DOCTOR_STATUS_OVERRIDE')")
    public ResponseEntity<DoctorStatusResponse> override(@Valid @RequestBody DoctorStatusOverrideRequest request) {
        return ResponseEntity.ok(statusService.overrideStatus(request));
    }
}
