package com.lockdoc.app.service.op;

import com.lockdoc.app.config.SecurityUtils;
import com.lockdoc.app.dto.op.ApprovalPolicyRequest;
import com.lockdoc.app.dto.op.ApprovalPolicyResponse;
import com.lockdoc.app.entity.Facility;
import com.lockdoc.app.entity.op.ApprovalPolicy;
import com.lockdoc.app.exception.ResourceNotFoundException;
import com.lockdoc.app.repository.FacilityRepository;
import com.lockdoc.app.repository.op.ApprovalPolicyRepository;
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
