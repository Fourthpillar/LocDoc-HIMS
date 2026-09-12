package com.lockdoc.app.service;

import com.lockdoc.app.config.SecurityUtils;
import com.lockdoc.app.dto.FacilityProfileRequest;
import com.lockdoc.app.dto.FacilityRequest;
import com.lockdoc.app.dto.FacilityResponse;
import com.lockdoc.app.dto.PageResponse;
import com.lockdoc.app.entity.Facility;
import com.lockdoc.app.exception.DuplicateResourceException;
import com.lockdoc.app.exception.InvalidDocumentStateException;
import com.lockdoc.app.exception.ResourceNotFoundException;
import com.lockdoc.app.repository.FacilityRepository;
import com.lockdoc.app.service.platform.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Set;

/**
 * Super Admin's facility onboarding queue (Master Spec §10). Deliberately
 * the one service in this codebase that is NOT facility-scoped - it's
 * cross-facility by design, since onboarding and verifying facilities is
 * the one thing that has to happen before a facility boundary exists to
 * scope anything else against.
 *
 * Backend/API only at this build step (§16 step 2) - no screen yet; the
 * onboarding-queue UI lands once the app shell exists (step 3a).
 */
@Service
@RequiredArgsConstructor
@Transactional
public class FacilityService {

    private final FacilityRepository facilityRepository;
    private final AuditLogService auditLogService;

    public PageResponse<FacilityResponse> list(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Facility> result = StringUtils.hasText(search)
                ? facilityRepository.search(search, pageable)
                : facilityRepository.findAll(pageable);
        return PageResponse.of(result, FacilityResponse::toResponse);
    }

    public FacilityResponse get(Long id) {
        return FacilityResponse.toResponse(findEntity(id));
    }

    /** Facility self-registration (Master Spec §10) - starts PENDING, no modules active yet. */
    public FacilityResponse register(FacilityRequest request) {
        if (facilityRepository.findByNameIgnoreCase(request.getName()).isPresent()) {
            throw new DuplicateResourceException("Facility already exists with name: " + request.getName());
        }
        Facility facility = Facility.builder()
                .name(request.getName())
                .type(request.getType())
                .address(request.getAddress())
                .geoLat(request.getGeoLat())
                .geoLng(request.getGeoLng())
                .licenceNumber(request.getLicenceNumber())
                .verificationStatus(Facility.VERIFICATION_PENDING)
                .active(true)
                .build();
        return FacilityResponse.toResponse(facilityRepository.save(facility));
    }

    public FacilityResponse verify(Long id) {
        Facility facility = findEntity(id);
        if (!Facility.VERIFICATION_PENDING.equals(facility.getVerificationStatus())) {
            throw new InvalidDocumentStateException(
                    "Facility can only be verified from PENDING status, current status: " + facility.getVerificationStatus());
        }
        facility.setVerificationStatus(Facility.VERIFICATION_VERIFIED);
        FacilityResponse response = FacilityResponse.toResponse(facilityRepository.save(facility));
        auditLogService.record(facility.getId(), SecurityUtils.currentUserId(), "FACILITY_VERIFY", "Facility", facility.getId(), "PENDING", "VERIFIED");
        return response;
    }

    public FacilityResponse reject(Long id) {
        Facility facility = findEntity(id);
        if (!Facility.VERIFICATION_PENDING.equals(facility.getVerificationStatus())) {
            throw new InvalidDocumentStateException(
                    "Facility can only be rejected from PENDING status, current status: " + facility.getVerificationStatus());
        }
        facility.setVerificationStatus(Facility.VERIFICATION_REJECTED);
        FacilityResponse response = FacilityResponse.toResponse(facilityRepository.save(facility));
        auditLogService.record(facility.getId(), SecurityUtils.currentUserId(), "FACILITY_REJECT", "Facility", facility.getId(), "PENDING", "REJECTED");
        return response;
    }

    /**
     * Manual suspend/reinstate (Master Spec §10) - an operational action
     * independent of verificationStatus (a VERIFIED facility can still be
     * suspended, e.g. a licence issue surfacing post-onboarding). Applies
     * the same read-only-not-deleted rule as any other module downgrade
     * (§5 principle 2) - suspending sets active=false, which every
     * facility-scoped query already respects via the entity's own state,
     * not a delete.
     */
    public FacilityResponse suspend(Long id) {
        Facility facility = findEntity(id);
        facility.setActive(false);
        FacilityResponse response = FacilityResponse.toResponse(facilityRepository.save(facility));
        auditLogService.record(facility.getId(), SecurityUtils.currentUserId(), "FACILITY_SUSPEND", "Facility", facility.getId(), "active=true", "active=false");
        return response;
    }

    public FacilityResponse reinstate(Long id) {
        Facility facility = findEntity(id);
        facility.setActive(true);
        FacilityResponse response = FacilityResponse.toResponse(facilityRepository.save(facility));
        auditLogService.record(facility.getId(), SecurityUtils.currentUserId(), "FACILITY_REINSTATE", "Facility", facility.getId(), "active=false", "active=true");
        return response;
    }

    /** Module entitlement management (§10) - replaces the whole active-module set. */
    public FacilityResponse updateModules(Long id, Set<String> activeModules) {
        Facility facility = findEntity(id);
        String before = String.join(",", facility.getActiveModules());
        facility.setActiveModules(activeModules);
        FacilityResponse response = FacilityResponse.toResponse(facilityRepository.save(facility));
        auditLogService.record(facility.getId(), SecurityUtils.currentUserId(), "FACILITY_MODULES_UPDATE", "Facility", facility.getId(), before, String.join(",", activeModules));
        return response;
    }

    /**
     * Facility self-profile (Master Spec §17.7 #33b) - the facility's own
     * Hospital/Clinic Admin, not Super Admin, restricted to
     * {@code requireFacilityId()} so no facility can read/edit another's
     * profile through this endpoint. Editable any time after Super Admin
     * verification (source material PDF H-02) - not gated on it being
     * still PENDING the way onboarding fields are.
     */
    public FacilityResponse getMine() {
        return FacilityResponse.toResponse(findEntity(SecurityUtils.requireFacilityId()));
    }

    public FacilityResponse updateMine(FacilityProfileRequest request) {
        Facility facility = findEntity(SecurityUtils.requireFacilityId());
        facility.setName(request.getName());
        facility.setAddress(request.getAddress());
        facility.setGeoLat(request.getGeoLat());
        facility.setGeoLng(request.getGeoLng());
        facility.setLicenceNumber(request.getLicenceNumber());
        return FacilityResponse.toResponse(facilityRepository.save(facility));
    }

    private Facility findEntity(Long id) {
        return facilityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Facility not found with id: " + id));
    }
}
