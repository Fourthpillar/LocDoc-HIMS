package com.lockdoc.doctor.service;

import com.lockdoc.common.config.SecurityUtils;
import com.lockdoc.doctor.dto.ConsultationRateRejectRequest;
import com.lockdoc.doctor.dto.ConsultationRateRequest;
import com.lockdoc.doctor.dto.ConsultationRateResponse;
import com.lockdoc.common.entity.Doctor;
import com.lockdoc.common.entity.Facility;
import com.lockdoc.common.entity.ConsultationRate;
import com.lockdoc.common.entity.DoctorFacilityMapping;
import com.lockdoc.common.exception.InvalidDocumentStateException;
import com.lockdoc.common.exception.ResourceNotFoundException;
import com.lockdoc.common.repository.DoctorRepository;
import com.lockdoc.common.service.CurrentDoctorService;
import com.lockdoc.common.repository.FacilityRepository;
import com.lockdoc.common.repository.ConsultationRateRepository;
import com.lockdoc.common.repository.DoctorFacilityMappingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Doctor-proposed consultation fee (Master Spec §8.6) — propose half only
 * (build order step 4); approve lands with step 5. A doctor can propose a
 * revision at any time, which re-enters PENDING_APPROVAL — this service
 * never mutates a prior proposal, each call is a new row, so the full
 * Pending/Approved/Rejected history stays visible on the doctor's own
 * screen (§17.6's ConsultationFeeProposalCard).
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ConsultationRateService {

    private final ConsultationRateRepository rateRepository;
    private final DoctorFacilityMappingRepository mappingRepository;
    private final DoctorRepository doctorRepository;
    private final CurrentDoctorService currentDoctorService;
    private final FacilityRepository facilityRepository;

    public ConsultationRateResponse propose(ConsultationRateRequest request) {
        Long userId = SecurityUtils.currentUserId();
        Doctor doctor = currentDoctorService.require();

        boolean mapped = !mappingRepository
                .findByDoctorIdAndFacilityIdAndStatusIn(doctor.getId(), request.getFacilityId(), List.of(DoctorFacilityMapping.STATUS_ACCEPTED))
                .isEmpty();
        if (!mapped) {
            throw new InvalidDocumentStateException("You are not currently mapped to this facility - cannot propose a fee");
        }

        Facility facility = facilityRepository.findById(request.getFacilityId())
                .orElseThrow(() -> new ResourceNotFoundException("Facility not found with id: " + request.getFacilityId()));

        ConsultationRate rate = ConsultationRate.builder()
                .doctor(doctor)
                .facility(facility)
                .orgType(StringUtils.hasText(request.getOrgType()) ? request.getOrgType() : ConsultationRate.ORG_TYPE_DIRECT)
                .dayNightIndicator(StringUtils.hasText(request.getDayNightIndicator()) ? request.getDayNightIndicator() : ConsultationRate.DAY)
                .totalAmount(request.getTotalAmount())
                .hospitalPercent(request.getHospitalPercent())
                .status(ConsultationRate.STATUS_PENDING_APPROVAL)
                .proposedByUserId(userId)
                .build();
        return ConsultationRateResponse.toResponse(rateRepository.save(rate));
    }

    /** The doctor's own proposals, newest first — optionally narrowed to one facility. */
    public List<ConsultationRateResponse> myProposals(Long facilityId) {
        Doctor doctor = currentDoctorService.require();

        List<ConsultationRate> rates = facilityId != null
                ? rateRepository.findByDoctorIdAndFacilityIdOrderByCreatedDateDesc(doctor.getId(), facilityId)
                : rateRepository.findByDoctorIdOrderByCreatedDateDesc(doctor.getId());

        return rates.stream().map(ConsultationRateResponse::toResponse).toList();
    }

    /**
     * The approve half of §8.6, completing build order step 4 — this is
     * build order step 5's own piece of the same flow, not a new one.
     */
    public List<ConsultationRateResponse> pendingForFacility() {
        Long facilityId = SecurityUtils.requireFacilityId();
        return rateRepository.findByFacilityIdAndStatusOrderByCreatedDateDesc(facilityId, ConsultationRate.STATUS_PENDING_APPROVAL)
                .stream().map(ConsultationRateResponse::toResponse).toList();
    }

    public ConsultationRateResponse approve(Long id) {
        Long facilityId = SecurityUtils.requireFacilityId();
        Long userId = SecurityUtils.currentUserId();
        ConsultationRate rate = findOwnedByFacility(id, facilityId);
        requirePending(rate);
        rate.setStatus(ConsultationRate.STATUS_APPROVED);
        rate.setApprovedByUserId(userId);
        rate.setApprovedAt(java.time.LocalDateTime.now());
        rate.setEffectiveFrom(java.time.LocalDate.now());
        return ConsultationRateResponse.toResponse(rateRepository.save(rate));
    }

    public ConsultationRateResponse reject(Long id, ConsultationRateRejectRequest request) {
        Long facilityId = SecurityUtils.requireFacilityId();
        Long userId = SecurityUtils.currentUserId();
        ConsultationRate rate = findOwnedByFacility(id, facilityId);
        requirePending(rate);
        rate.setStatus(ConsultationRate.STATUS_REJECTED);
        rate.setApprovedByUserId(userId);
        rate.setApprovedAt(java.time.LocalDateTime.now());
        rate.setRejectionReason(request.getReason());
        return ConsultationRateResponse.toResponse(rateRepository.save(rate));
    }

    private ConsultationRate findOwnedByFacility(Long id, Long facilityId) {
        ConsultationRate rate = rateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consultation rate proposal not found with id: " + id));
        if (!rate.getFacility().getId().equals(facilityId)) {
            throw new ResourceNotFoundException("Consultation rate proposal not found with id: " + id);
        }
        return rate;
    }

    private void requirePending(ConsultationRate rate) {
        if (!ConsultationRate.STATUS_PENDING_APPROVAL.equals(rate.getStatus())) {
            throw new InvalidDocumentStateException(
                    "Consultation rate can only be resolved from PENDING_APPROVAL, current status: " + rate.getStatus());
        }
    }
}
