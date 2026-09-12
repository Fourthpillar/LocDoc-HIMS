package com.lockdoc.app.repository.dataprotection;

import com.lockdoc.app.entity.dataprotection.ErasureRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ErasureRequestRepository extends JpaRepository<ErasureRequest, Long> {
    List<ErasureRequest> findByFacilityIdOrderByRequestedAtAsc(Long facilityId);
    Optional<ErasureRequest> findByIdAndFacilityId(Long id, Long facilityId);
}
