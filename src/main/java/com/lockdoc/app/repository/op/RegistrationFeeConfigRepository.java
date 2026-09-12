package com.lockdoc.app.repository.op;

import com.lockdoc.app.entity.op.RegistrationFeeConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RegistrationFeeConfigRepository extends JpaRepository<RegistrationFeeConfig, Long> {
    Optional<RegistrationFeeConfig> findByFacilityId(Long facilityId);
}
