package com.lockdoc.app.repository.op;

import com.lockdoc.app.entity.op.Cancellation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CancellationRepository extends JpaRepository<Cancellation, Long> {
    List<Cancellation> findByFacilityIdAndStatusOrderByCreatedDateAsc(Long facilityId, String status);
    Optional<Cancellation> findByIdAndFacilityId(Long id, Long facilityId);
    Optional<Cancellation> findByEntityTypeAndEntityIdAndStatus(String entityType, Long entityId, String status);

    /** Cancellations report (§17.7 #12a). */
    List<Cancellation> findByFacilityIdAndCreatedDateBetweenOrderByCreatedDateAsc(Long facilityId, LocalDateTime from, LocalDateTime to);
}
