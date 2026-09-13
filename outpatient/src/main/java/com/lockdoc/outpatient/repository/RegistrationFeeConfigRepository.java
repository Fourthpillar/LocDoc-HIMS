package com.lockdoc.outpatient.repository;

import com.lockdoc.outpatient.entity.RegistrationFeeConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RegistrationFeeConfigRepository extends JpaRepository<RegistrationFeeConfig, Long> {
    Optional<RegistrationFeeConfig> findByFacilityId(Long facilityId);
}
