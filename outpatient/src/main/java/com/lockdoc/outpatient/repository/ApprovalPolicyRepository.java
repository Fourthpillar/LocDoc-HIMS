package com.lockdoc.outpatient.repository;

import com.lockdoc.outpatient.entity.ApprovalPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApprovalPolicyRepository extends JpaRepository<ApprovalPolicy, Long> {
    List<ApprovalPolicy> findByFacilityId(Long facilityId);
    Optional<ApprovalPolicy> findByFacilityIdAndApprovalType(Long facilityId, String approvalType);
}
