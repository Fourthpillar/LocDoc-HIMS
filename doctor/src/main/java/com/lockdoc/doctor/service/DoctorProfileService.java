package com.lockdoc.doctor.service;

import com.lockdoc.common.config.SecurityUtils;
import com.lockdoc.doctor.dto.DoctorProfileResponse;
import com.lockdoc.doctor.dto.DoctorProfileUpdateRequest;
import com.lockdoc.doctor.entity.Doctor;
import com.lockdoc.common.entity.User;
import com.lockdoc.doctor.entity.DoctorFacilityMapping;
import com.lockdoc.common.exception.ResourceNotFoundException;
import com.lockdoc.doctor.repository.DoctorRepository;
import com.lockdoc.doctor.repository.DoctorFacilityMappingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Doctor profile, self-service (Master Spec §8.9, screen #19). */
@Service
@RequiredArgsConstructor
@Transactional
public class DoctorProfileService {

    private final DoctorRepository doctorRepository;
    private final DoctorFacilityMappingRepository mappingRepository;

    public DoctorProfileResponse getMine() {
        Doctor doctor = currentDoctor();
        return toResponse(doctor);
    }

    public DoctorProfileResponse updateMine(DoctorProfileUpdateRequest request) {
        Doctor doctor = currentDoctor();
        doctor.setSpecialties(request.getSpecialties());
        doctor.setQualifications(request.getQualifications());
        return toResponse(doctorRepository.save(doctor));
    }

    private DoctorProfileResponse toResponse(Doctor doctor) {
        User user = doctor.getUser();
        var facilities = mappingRepository.findByDoctorIdAndStatus(doctor.getId(), DoctorFacilityMapping.STATUS_ACCEPTED).stream()
                .map(m -> m.getFacility().getName())
                .toList();
        return DoctorProfileResponse.builder()
                .id(doctor.getId())
                .fullName(doctor.getFullName())
                .registrationNumber(doctor.getRegistrationNumber())
                .verificationStatus(doctor.getVerificationStatus())
                .specialties(doctor.getSpecialties())
                .qualifications(doctor.getQualifications())
                .username(user.getUsername())
                .email(user.getEmail())
                .facilitiesPractisedAt(facilities)
                .build();
    }

    private Doctor currentDoctor() {
        Long userId = SecurityUtils.currentUserId();
        return doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No Doctor profile for the current user"));
    }
}
