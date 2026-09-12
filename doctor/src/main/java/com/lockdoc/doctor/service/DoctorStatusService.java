package com.lockdoc.doctor.service;

import com.lockdoc.common.config.SecurityUtils;
import com.lockdoc.doctor.dto.DoctorStatusOverrideRequest;
import com.lockdoc.doctor.dto.DoctorStatusRequest;
import com.lockdoc.doctor.dto.DoctorStatusResponse;
import com.lockdoc.doctor.dto.DoctorStatusTodayResponse;
import com.lockdoc.doctor.entity.Doctor;
import com.lockdoc.common.entity.Facility;
import com.lockdoc.doctor.entity.DoctorFacilityMapping;
import com.lockdoc.doctor.entity.DoctorStatus;
import com.lockdoc.doctor.exception.InvalidDocumentStateException;
import com.lockdoc.common.exception.ResourceNotFoundException;
import com.lockdoc.doctor.repository.DoctorRepository;
import com.lockdoc.common.repository.FacilityRepository;
import com.lockdoc.doctor.repository.DoctorFacilityMappingRepository;
import com.lockdoc.doctor.repository.DoctorStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Live status — build order step 4's own named deliverable (Master Spec
 * §8.3): manual, doctor-set, facility/session-scoped, no GPS. Reception's
 * override (source=RECEPTION) is explicitly deferred to step 5, where the
 * front-desk day list it lives on actually exists.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class DoctorStatusService {

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    private static final Set<String> VALID_STATUSES = Set.of(
            DoctorStatus.UNAVAILABLE, DoctorStatus.AVAILABLE, DoctorStatus.IN_TRANSIT,
            DoctorStatus.AT_FACILITY, DoctorStatus.IN_CONSULTATION, DoctorStatus.IN_ROUNDS, DoctorStatus.DAY_COMPLETE);

    private final DoctorStatusRepository statusRepository;
    private final DoctorFacilityMappingRepository mappingRepository;
    private final DoctorRepository doctorRepository;
    private final FacilityRepository facilityRepository;

    public DoctorStatusResponse setStatus(DoctorStatusRequest request) {
        if (!VALID_STATUSES.contains(request.getStatus())) {
            throw new InvalidDocumentStateException("Unknown status: " + request.getStatus());
        }

        Long userId = SecurityUtils.currentUserId();
        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No Doctor profile for the current user"));

        boolean mapped = !mappingRepository
                .findByDoctorIdAndFacilityIdAndStatusIn(doctor.getId(), request.getFacilityId(), List.of(DoctorFacilityMapping.STATUS_ACCEPTED))
                .isEmpty();
        if (!mapped) {
            throw new InvalidDocumentStateException("You are not currently mapped to this facility");
        }

        Facility facility = facilityRepository.findById(request.getFacilityId())
                .orElseThrow(() -> new ResourceNotFoundException("Facility not found with id: " + request.getFacilityId()));

        DoctorStatus status = DoctorStatus.builder()
                .doctor(doctor)
                .facility(facility)
                .sessionDate(LocalDate.now(IST))
                .status(request.getStatus())
                .source(DoctorStatus.SOURCE_SELF)
                .setByUserId(userId)
                .build();
        return DoctorStatusResponse.toResponse(statusRepository.save(status));
    }

    /** Today's status across every facility the doctor is ACCEPTED at — the doctor home screen's (#13) main feed. */
    public List<DoctorStatusTodayResponse> myStatusToday() {
        Long userId = SecurityUtils.currentUserId();
        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No Doctor profile for the current user"));

        List<DoctorFacilityMapping> acceptedMappings =
                mappingRepository.findByDoctorIdAndStatus(doctor.getId(), DoctorFacilityMapping.STATUS_ACCEPTED);

        LocalDate today = LocalDate.now(IST);
        List<DoctorStatus> todaysStatuses = statusRepository.findByDoctorIdAndSessionDateOrderByCreatedDateDesc(doctor.getId(), today);

        return acceptedMappings.stream()
                .map(mapping -> {
                    DoctorStatus latest = todaysStatuses.stream()
                            .filter(s -> s.getFacility().getId().equals(mapping.getFacility().getId()))
                            .max(Comparator.comparing(DoctorStatus::getCreatedDate))
                            .orElse(null);
                    return DoctorStatusTodayResponse.builder()
                            .mappingId(mapping.getId())
                            .facilityId(mapping.getFacility().getId())
                            .facilityName(mapping.getFacility().getName())
                            .status(latest != null ? latest.getStatus() : DoctorStatus.UNAVAILABLE)
                            .source(latest != null ? latest.getSource() : null)
                            .lastUpdated(latest != null ? latest.getCreatedDate() : null)
                            .build();
                })
                .toList();
    }

    /**
     * Reception's override (Master Spec §8.3, build order step 5) - the
     * front-desk day list's status-override control. Unlike
     * {@link #setStatus}, the target doctor is not the caller, so this
     * validates the ACCEPTED mapping the same way but keyed off the
     * facility (the caller's own, via requireFacilityId()) rather than the
     * caller's own Doctor profile - a Receptionist has none.
     */
    public DoctorStatusResponse overrideStatus(DoctorStatusOverrideRequest request) {
        if (!VALID_STATUSES.contains(request.getStatus())) {
            throw new InvalidDocumentStateException("Unknown status: " + request.getStatus());
        }

        Long facilityId = SecurityUtils.requireFacilityId();
        Long userId = SecurityUtils.currentUserId();

        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + request.getDoctorId()));

        boolean mapped = !mappingRepository
                .findByDoctorIdAndFacilityIdAndStatusIn(doctor.getId(), facilityId, List.of(DoctorFacilityMapping.STATUS_ACCEPTED))
                .isEmpty();
        if (!mapped) {
            throw new InvalidDocumentStateException("This doctor is not currently mapped to your facility");
        }

        Facility facility = facilityRepository.findById(facilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Facility not found with id: " + facilityId));

        DoctorStatus status = DoctorStatus.builder()
                .doctor(doctor)
                .facility(facility)
                .sessionDate(LocalDate.now(IST))
                .status(request.getStatus())
                .source(DoctorStatus.SOURCE_RECEPTION)
                .setByUserId(userId)
                .build();
        return DoctorStatusResponse.toResponse(statusRepository.save(status));
    }

    /**
     * Hospital/Clinic Admin's view of every doctor currently status-active
     * at their facility today. Backend only, per build order step 4 — no
     * screen consumes this until step 5's front-desk day list.
     */
    public List<DoctorStatusResponse> facilityStatusToday() {
        Long facilityId = SecurityUtils.requireFacilityId();
        LocalDate today = LocalDate.now(IST);
        List<DoctorStatus> all = statusRepository.findByFacilityIdAndSessionDateOrderByCreatedDateDesc(facilityId, today);

        // Latest row per doctor — `all` is already createdDate-desc, so the first occurrence per doctor wins.
        return all.stream()
                .collect(java.util.stream.Collectors.toMap(
                        s -> s.getDoctor().getId(), s -> s, (first, later) -> first, java.util.LinkedHashMap::new))
                .values().stream()
                .map(DoctorStatusResponse::toResponse)
                .toList();
    }
}
