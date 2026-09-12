package com.lockdoc.app.repository.op;

import com.lockdoc.app.entity.op.CommissionBasis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommissionBasisRepository extends JpaRepository<CommissionBasis, Long> {
    List<CommissionBasis> findByFacilityIdAndActiveTrue(Long facilityId);
    Optional<CommissionBasis> findByIdAndFacilityId(Long id, Long facilityId);
}
