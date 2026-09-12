package com.lockdoc.app.service.doctor;

import com.lockdoc.app.config.SecurityUtils;
import com.lockdoc.app.dto.doctor.ConsultationRateRejectRequest;
import com.lockdoc.app.dto.doctor.ConsultationRateRequest;
import com.lockdoc.app.dto.doctor.ConsultationRateResponse;
import com.lockdoc.app.entity.Doctor;
import com.lockdoc.app.entity.Facility;
import com.lockdoc.app.entity.doctor.ConsultationRate;
import com.lockdoc.app.entity.doctor.DoctorFacilityMapping;
import com.lockdoc.app.exception.InvalidDocumentStateException;
import com.lockdoc.app.exception.ResourceNotFoundException;
import com.lockdoc.app.repository.DoctorRepository;
import com.lockdoc.app.repository.FacilityRepository;
import com.lockdoc.app.repository.doctor.ConsultationRateRepository;
import com.lockdoc.app.repository.doctor.DoctorFacilityMappingRepository;
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
    private final FacilityRepository facilityRepository;

    public ConsultationRateResponse propose(ConsultationRateRequest request) {
        Long userId = SecurityUtils.currentUserId();
        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No Doctor profile for the current user"));

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
        Long userId = SecurityUtils.currentUserId();
        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No Doctor profile for the current user"));

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
