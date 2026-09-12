package com.lockdoc.app.service.doctor;

import com.lockdoc.app.config.SecurityUtils;
import com.lockdoc.app.dto.doctor.FreeReviewPolicyRequest;
import com.lockdoc.app.dto.doctor.FreeReviewPolicyResponse;
import com.lockdoc.app.entity.Doctor;
import com.lockdoc.app.entity.Facility;
import com.lockdoc.app.entity.doctor.DoctorFacilityMapping;
import com.lockdoc.app.entity.doctor.FreeReviewPolicy;
import com.lockdoc.app.exception.InvalidDocumentStateException;
import com.lockdoc.app.exception.ResourceNotFoundException;
import com.lockdoc.app.repository.DoctorRepository;
import com.lockdoc.app.repository.FacilityRepository;
import com.lockdoc.app.repository.doctor.DoctorFacilityMappingRepository;
import com.lockdoc.app.repository.doctor.FreeReviewPolicyRepository;
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
        Long facilityId = SecurityUtils.requireFacilityId();
        return policyRepository.findByFacilityId(facilityId).stream().map(FreeReviewPolicyResponse::toResponse).toList();
    }

    public FreeReviewPolicyResponse upsert(Long doctorId, FreeReviewPolicyRequest request) {
        Long facilityId = SecurityUtils.requireFacilityId();
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + doctorId));
        boolean mapped = !mappingRepository
                .findByDoctorIdAndFacilityIdAndStatusIn(doctorId, facilityId, List.of(DoctorFacilityMapping.STATUS_ACCEPTED))
                .isEmpty();
        if (!mapped) {
            throw new InvalidDocumentStateException("This doctor is not currently mapped to your facility");
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

    public void delete(Long doctorId) {
        Long facilityId = SecurityUtils.requireFacilityId();
        FreeReviewPolicy policy = policyRepository.findByDoctorIdAndFacilityId(doctorId, facilityId)
                .orElseThrow(() -> new ResourceNotFoundException("No free-review policy set for this doctor"));
        policyRepository.delete(policy);
    }
}
