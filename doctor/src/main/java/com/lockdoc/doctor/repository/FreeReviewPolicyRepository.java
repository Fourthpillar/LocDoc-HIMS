package com.lockdoc.doctor.repository;

import com.lockdoc.doctor.entity.FreeReviewPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FreeReviewPolicyRepository extends JpaRepository<FreeReviewPolicy, Long> {

    Optional<FreeReviewPolicy> findByDoctorIdAndFacilityId(Long doctorId, Long facilityId);

    List<FreeReviewPolicy> findByFacilityId(Long facilityId);

    /** Every facility's policies — the cross-facility read Super Admin gets (SecurityUtils.facilityScopeOrAll). */
    List<FreeReviewPolicy> findAllByOrderByIdAsc();
}
