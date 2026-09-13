package com.lockdoc.common.repository.platform;

import com.lockdoc.common.entity.platform.AuditLogEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditLogRepository extends JpaRepository<AuditLogEntry, Long> {

    Page<AuditLogEntry> findAllByOrderByOccurredAtDesc(Pageable pageable);

    @Query("SELECT a FROM AuditLogEntry a WHERE "
            + "(:facilityId IS NULL OR a.facilityId = :facilityId) AND "
            + "(:action IS NULL OR a.action = :action) "
            + "ORDER BY a.occurredAt DESC")
    Page<AuditLogEntry> search(@Param("facilityId") Long facilityId, @Param("action") String action, Pageable pageable);
}
