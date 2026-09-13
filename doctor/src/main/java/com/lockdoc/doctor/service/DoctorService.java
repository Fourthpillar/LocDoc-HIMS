package com.lockdoc.doctor.service;


import com.lockdoc.common.service.FacilityService;
import com.lockdoc.common.config.SecurityUtils;
import com.lockdoc.doctor.dto.DoctorRegisterRequest;
import com.lockdoc.doctor.dto.DoctorResponse;
import com.lockdoc.common.dto.PageResponse;
import com.lockdoc.common.entity.Doctor;
import com.lockdoc.common.entity.Role;
import com.lockdoc.common.entity.User;
import com.lockdoc.common.exception.DuplicateResourceException;
import com.lockdoc.common.exception.InvalidDocumentStateException;
import com.lockdoc.common.exception.ResourceNotFoundException;
import com.lockdoc.common.repository.DoctorRepository;
import com.lockdoc.common.repository.RoleRepository;
import com.lockdoc.common.repository.UserRepository;
import com.lockdoc.common.service.platform.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Set;

/**
 * Doctor self-registration and Super Admin verification (Master Spec §10,
 * build order §16 step 3) - the module every earlier LocDoc-HIMS document
 * excluded entirely, restored here as the account-provisioning half of it
 * (the availability/status/prescription functionality itself is later
 * steps).
 *
 * Cross-facility by design, like FacilityService - a doctor's identity
 * isn't scoped to a facility (Master Spec §4), so neither is this.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    public PageResponse<DoctorResponse> list(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Doctor> result = StringUtils.hasText(search)
                ? doctorRepository.search(search, pageable)
                : doctorRepository.findAll(pageable);
        return PageResponse.of(result, DoctorResponse::toResponse);
    }

    public DoctorResponse get(Long id) {
        return DoctorResponse.toResponse(findEntity(id));
    }

    /**
     * Creates the account disabled - see DoctorRegisterRequest javadoc for
     * why credential collection happens here rather than at verify().
     */
    public DoctorResponse register(DoctorRegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already taken: " + request.getUsername());
        }
        if (doctorRepository.existsByRegistrationNumber(request.getRegistrationNumber())) {
            throw new DuplicateResourceException("A doctor is already registered with registration number: " + request.getRegistrationNumber());
        }

        Role doctorRole = roleRepository.findByRoleCode("DOCTOR")
                .orElseThrow(() -> new IllegalStateException("DOCTOR role is not seeded - see V11"));

        // facility(null) - a doctor practises at zero-or-more facilities via
        // DoctorFacilityMapping, never exactly one; enabled(false) - stays
        // locked out until Super Admin verifies (below).
        User user = User.builder()
                .facility(null)
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .fullName(request.getFullName())
                .enabled(false)
                .accountNonLocked(true)
                .roles(Set.of(doctorRole))
                .build();
        user = userRepository.save(user);

        Doctor doctor = Doctor.builder()
                .user(user)
                .fullName(request.getFullName())
                .registrationNumber(request.getRegistrationNumber())
                .specialties(request.getSpecialties())
                .verificationStatus(Doctor.VERIFICATION_PENDING)
                .build();
        return DoctorResponse.toResponse(doctorRepository.save(doctor));
    }

    /** Verifying is what actually unlocks login (User.enabled), not registration. */
    public DoctorResponse verify(Long id) {
        Doctor doctor = findEntity(id);
        if (!Doctor.VERIFICATION_PENDING.equals(doctor.getVerificationStatus())) {
            throw new InvalidDocumentStateException(
                    "Doctor can only be verified from PENDING status, current status: " + doctor.getVerificationStatus());
        }
        doctor.setVerificationStatus(Doctor.VERIFICATION_VERIFIED);
        doctorRepository.save(doctor);

        User user = doctor.getUser();
        user.setEnabled(true);
        userRepository.save(user);

        auditLogService.record(null, SecurityUtils.currentUserId(), "DOCTOR_VERIFY", "Doctor", doctor.getId(), "PENDING", "VERIFIED");
        return DoctorResponse.toResponse(doctor);
    }

    public DoctorResponse reject(Long id) {
        Doctor doctor = findEntity(id);
        if (!Doctor.VERIFICATION_PENDING.equals(doctor.getVerificationStatus())) {
            throw new InvalidDocumentStateException(
                    "Doctor can only be rejected from PENDING status, current status: " + doctor.getVerificationStatus());
        }
        doctor.setVerificationStatus(Doctor.VERIFICATION_REJECTED);
        // Stays disabled - a rejected doctor's account never logs in.
        DoctorResponse response = DoctorResponse.toResponse(doctorRepository.save(doctor));
        auditLogService.record(null, SecurityUtils.currentUserId(), "DOCTOR_REJECT", "Doctor", doctor.getId(), "PENDING", "REJECTED");
        return response;
    }

    private Doctor findEntity(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + id));
    }
}
