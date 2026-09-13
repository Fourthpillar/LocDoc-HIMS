package com.lockdoc.outpatient.repository;

import com.lockdoc.outpatient.entity.CounterSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CounterSessionRepository extends JpaRepository<CounterSession, Long> {
    Optional<CounterSession> findByFacilityIdAndCounterTypeAndStatus(Long facilityId, String counterType, String status);
    List<CounterSession> findByFacilityIdAndCounterTypeOrderByOpenedAtDesc(Long facilityId, String counterType);
    Optional<CounterSession> findByIdAndFacilityId(Long id, Long facilityId);
}
