package com.lockdoc.outpatient.service;

import com.lockdoc.common.config.SecurityUtils;
import com.lockdoc.outpatient.dto.ApprovalPolicyRequest;
import com.lockdoc.outpatient.dto.ApprovalPolicyResponse;
import com.lockdoc.common.entity.Facility;
import com.lockdoc.outpatient.entity.ApprovalPolicy;
import com.lockdoc.common.exception.ResourceNotFoundException;
import com.lockdoc.common.repository.FacilityRepository;
import com.lockdoc.outpatient.repository.ApprovalPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Self-service (Master Spec §7.5/§15.1) - Hospital/Clinic Admin's own threshold config. */
@Service
@RequiredArgsConstructor
@Transactional
public class ApprovalPolicyService {

    private final ApprovalPolicyRepository policyRepository;
    private final FacilityRepository facilityRepository;

    public List<ApprovalPolicyResponse> listMine() {
        Long facilityId = SecurityUtils.requireFacilityId();
        return policyRepository.findByFacilityId(facilityId).stream().map(ApprovalPolicyResponse::toResponse).toList();
    }

    public ApprovalPolicyResponse upsert(ApprovalPolicyRequest request) {
        Long facilityId = SecurityUtils.requireFacilityId();
        ApprovalPolicy policy = policyRepository.findByFacilityIdAndApprovalType(facilityId, request.getApprovalType())
                .orElseGet(() -> {
                    Facility facility = facilityRepository.findById(facilityId)
                            .orElseThrow(() -> new ResourceNotFoundException("Facility not found with id: " + facilityId));
                    return ApprovalPolicy.builder().facility(facility).approvalType(request.getApprovalType()).build();
                });
        policy.setThresholdValue(request.getThresholdValue());
        return ApprovalPolicyResponse.toResponse(policyRepository.save(policy));
    }
}
