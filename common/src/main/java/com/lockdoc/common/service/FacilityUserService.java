package com.lockdoc.common.service;

import com.lockdoc.common.config.SecurityUtils;
import com.lockdoc.common.dto.FacilityUserRequest;
import com.lockdoc.common.dto.FacilityUserResponse;
import com.lockdoc.common.entity.Facility;
import com.lockdoc.common.entity.Role;
import com.lockdoc.common.entity.User;
import com.lockdoc.common.exception.DuplicateResourceException;
import com.lockdoc.common.exception.InvalidDocumentStateException;
import com.lockdoc.common.exception.ResourceNotFoundException;
import com.lockdoc.common.repository.FacilityRepository;
import com.lockdoc.common.repository.RoleRepository;
import com.lockdoc.common.repository.UserRepository;
import com.lockdoc.common.service.platform.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * Manage Users (Master Spec §17.7 #33a) - "No screen existed for this
 * despite §4.1's permission matrix explicitly stating the capability":
 * Hospital/Clinic Admin creates/deactivates Receptionist
 * accounts, and resets the forced-password-change flag, at their own
 * facility only. Deliberately cannot touch Hospital Admin, Doctor, or
 * Super Admin accounts through this endpoint - those are provisioned
 * elsewhere (bootstrap, self-registration, platform onboarding).
 */
@Service
@RequiredArgsConstructor
@Transactional
public class FacilityUserService {

    private static final List<String> MANAGEABLE_ROLES = List.of("RECEPTIONIST");

    private final UserRepository userRepository;
    private final FacilityRepository facilityRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    public List<FacilityUserResponse> list() {
        Long facilityId = SecurityUtils.requireFacilityId();
        return userRepository.findByFacilityIdAndRoles_RoleCodeInOrderByFullNameAsc(facilityId, MANAGEABLE_ROLES).stream()
                .map(FacilityUserResponse::toResponse)
                .toList();
    }

    public FacilityUserResponse create(FacilityUserRequest request) {
        if (!MANAGEABLE_ROLES.contains(request.getRoleCode())) {
            throw new InvalidDocumentStateException("roleCode must be one of " + MANAGEABLE_ROLES + " - use platform onboarding for other roles");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already taken: " + request.getUsername());
        }
        Long facilityId = SecurityUtils.requireFacilityId();
        Facility facility = facilityRepository.getReferenceById(facilityId);
        Role role = roleRepository.findByRoleCode(request.getRoleCode())
                .orElseThrow(() -> new IllegalStateException(request.getRoleCode() + " role is not seeded"));

        User user = User.builder()
                .facility(facility)
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .fullName(request.getFullName())
                .enabled(true)
                .accountNonLocked(true)
                .mustChangePassword(false)
                .roles(Set.of(role))
                .build();
        FacilityUserResponse response = FacilityUserResponse.toResponse(userRepository.save(user));
        auditLogService.record(facilityId, SecurityUtils.currentUserId(), "FACILITY_USER_CREATE", "User", response.getId(), null, request.getRoleCode() + ":" + request.getUsername());
        return response;
    }

    public FacilityUserResponse setEnabled(Long id, boolean enabled) {
        User user = findEntity(id);
        user.setEnabled(enabled);
        FacilityUserResponse response = FacilityUserResponse.toResponse(userRepository.save(user));
        auditLogService.record(SecurityUtils.requireFacilityId(), SecurityUtils.currentUserId(),
                enabled ? "FACILITY_USER_REACTIVATE" : "FACILITY_USER_DEACTIVATE", "User", id, null, null);
        return response;
    }

    /** Flags the account so the next login must set a new password (e.g. after an out-of-band reset). */
    public FacilityUserResponse requirePasswordChange(Long id) {
        User user = findEntity(id);
        user.setMustChangePassword(true);
        return FacilityUserResponse.toResponse(userRepository.save(user));
    }

    private User findEntity(Long id) {
        Long facilityId = SecurityUtils.requireFacilityId();
        User user = userRepository.findByIdAndFacilityId(id, facilityId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        boolean manageable = user.getRoles().stream().anyMatch(r -> MANAGEABLE_ROLES.contains(r.getRoleCode()));
        if (!manageable) {
            throw new InvalidDocumentStateException("This account is not a Receptionist account manageable from this screen");
        }
        return user;
    }
}
