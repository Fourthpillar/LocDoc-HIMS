package com.lockdoc.doctor.service;

import com.lockdoc.common.config.SecurityUtils;
import com.lockdoc.doctor.dto.FreeReviewPolicyRequest;
import com.lockdoc.doctor.dto.FreeReviewPolicyResponse;
import com.lockdoc.doctor.entity.Doctor;
import com.lockdoc.common.entity.Facility;
import com.lockdoc.doctor.entity.DoctorFacilityMapping;
import com.lockdoc.doctor.entity.FreeReviewPolicy;
import com.lockdoc.doctor.exception.InvalidDocumentStateException;
import com.lockdoc.common.exception.ResourceNotFoundException;
import com.lockdoc.doctor.repository.DoctorRepository;
import com.lockdoc.common.repository.FacilityRepository;
import com.lockdoc.doctor.repository.DoctorFacilityMappingRepository;
import com.lockdoc.doctor.repository.FreeReviewPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Free-review policy (Master Spec §7.2, §7.4) - Hospital/Clinic Admin sets it per mapped doctor at their own facility. */
@Service
@RequiredArgsConstructor
@Transactional
public class FreeReviewPolicyService {

    private final FreeReviewPolicyRepository policyRepository;
    private final DoctorRepository doctorRepository;
    private final DoctorFacilityMappingRepository mappingRepository;
    private final FacilityRepository facilityRepository;

    public List<FreeReviewPolicyResponse> listForFacility() {
        Long facilityId = SecurityUtils.facilityScopeOrAll();
        List<FreeReviewPolicy> policies = facilityId == null
                ? policyRepository.findAllByOrderByIdAsc()
                : policyRepository.findByFacilityId(facilityId);
        return policies.stream().map(FreeReviewPolicyResponse::toResponse).toList();
    }

    /**
      * A policy belongs to one doctor at one facility, so a caller with no facility of
      * their own (Super Admin) has to say which. If they don't, and the doctor practises
      * at exactly one facility, that is not ambiguous — use it; otherwise ask.
      */
    public FreeReviewPolicyResponse upsert(Long doctorId, FreeReviewPolicyRequest request) {
        Long facilityId = resolveFacilityFor(doctorId, request.getFacilityId());
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + doctorId));
        boolean mapped = !mappingRepository
                .findByDoctorIdAndFacilityIdAndStatusIn(doctorId, facilityId, List.of(DoctorFacilityMapping.STATUS_ACCEPTED))
                .isEmpty();
        if (!mapped) {
            throw new InvalidDocumentStateException("This doctor is not currently mapped to that facility");
        }

        FreeReviewPolicy policy = policyRepository.findByDoctorIdAndFacilityId(doctorId, facilityId).orElse(null);
        if (policy == null) {
            Facility facility = facilityRepository.getReferenceById(facilityId);
            policy = FreeReviewPolicy.builder().doctor(doctor).facility(facility).build();
        }
        policy.setMaxDays(request.getMaxDays());
        policy.setMaxVisits(request.getMaxVisits());
        return FreeReviewPolicyResponse.toResponse(policyRepository.save(policy));
    }

    /**
     * Removes one doctor's policy at one facility.
     *
     * A caller who belongs to a facility can only mean theirs. One who doesn't (Super
     * Admin) names it — and if they don't, this removes every facility's policy for that
     * doctor, which is destructive enough to be worth saying out loud rather than
     * guessing at.
     */
    public void delete(Long doctorId, Long requestedFacilityId) {
        Long scope = SecurityUtils.facilityScopeOrAll();
        Long facilityId = scope != null ? scope : requestedFacilityId;
        List<FreeReviewPolicy> policies = facilityId == null
                ? policyRepository.findAllByOrderByIdAsc().stream().filter(p -> p.getDoctor().getId().equals(doctorId)).toList()
                : policyRepository.findByDoctorIdAndFacilityId(doctorId, facilityId).map(List::of).orElse(List.of());
        if (policies.isEmpty()) {
            throw new ResourceNotFoundException("No free-review policy set for this doctor");
        }
        policyRepository.deleteAll(policies);
    }

    /**
     * Which facility a write lands in: the caller's own, or — for Super Admin — the one
     * they named, or the doctor's only one when there is no ambiguity to resolve.
     */
    private Long resolveFacilityFor(Long doctorId, Long requestedFacilityId) {
        Long scope = SecurityUtils.facilityScopeOrAll();
        if (scope != null) {
            return scope;
        }
        if (requestedFacilityId != null) {
            return requestedFacilityId;
        }
        List<DoctorFacilityMapping> accepted =
                mappingRepository.findByDoctorIdAndStatus(doctorId, DoctorFacilityMapping.STATUS_ACCEPTED);
        if (accepted.size() == 1) {
            return accepted.get(0).getFacility().getId();
        }
        throw new InvalidDocumentStateException(accepted.isEmpty()
                ? "This doctor does not practise at any facility yet"
                : "This doctor practises at several facilities - say which one this policy is for (facilityId)");
    }
}
