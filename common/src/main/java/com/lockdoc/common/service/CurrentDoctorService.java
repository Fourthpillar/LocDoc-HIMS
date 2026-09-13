package com.lockdoc.common.service;

import com.lockdoc.common.config.AppUserPrincipal;
import com.lockdoc.common.config.SecurityUtils;
import com.lockdoc.common.entity.Doctor;
import com.lockdoc.common.exception.ResourceNotFoundException;
import com.lockdoc.common.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Resolves "the doctor this request is acting as" for every doctor
 * self-service operation: the Doctor profile linked to the logged-in user, or,
 * for Super Admin, the doctor picked in the UI's module switcher (see
 * {@link AppUserPrincipal} - acting context). One place, so no doctor service
 * can forget the Super Admin path.
 */
@Service
@RequiredArgsConstructor
public class CurrentDoctorService {

    private final DoctorRepository doctorRepository;

    public Doctor require() {
        AppUserPrincipal principal = SecurityUtils.currentPrincipal();
        if (principal.isSuperAdmin()) {
            Long actingDoctorId = principal.getActingDoctorId();
            if (actingDoctorId == null) {
                throw new IllegalStateException(
                        "This operation acts as a doctor. Super Admin: select a doctor in the module switcher first.");
            }
            return doctorRepository.findById(actingDoctorId)
                    .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + actingDoctorId));
        }
        return doctorRepository.findByUserId(principal.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("No Doctor profile for the current user"));
    }
}
