package com.lockdoc.app.repository.doctor;

import com.lockdoc.app.entity.doctor.FreeReviewPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FreeReviewPolicyRepository extends JpaRepository<FreeReviewPolicy, Long> {

    Optional<FreeReviewPolicy> findByDoctorIdAndFacilityId(Long doctorId, Long facilityId);

    List<FreeReviewPolicy> findByFacilityId(Long facilityId);
}
