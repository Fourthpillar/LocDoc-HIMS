package com.lockdoc.app.repository.op;

import com.lockdoc.app.entity.op.ReferralDoctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReferralDoctorRepository extends JpaRepository<ReferralDoctor, Long> {
    List<ReferralDoctor> findByFacilityIdAndActiveTrueOrderByNameAsc(Long facilityId);
    Optional<ReferralDoctor> findByIdAndFacilityId(Long id, Long facilityId);
}
