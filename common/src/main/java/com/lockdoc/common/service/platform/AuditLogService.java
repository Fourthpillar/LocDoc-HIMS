package com.lockdoc.common.service.platform;

import com.lockdoc.common.entity.platform.AuditLogEntry;
import com.lockdoc.common.repository.platform.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cross-facility audit log (Master Spec §10, §6, screen #42).
 *
 * <b>Honest scope statement:</b> {@link #record} is called explicitly
 * from a deliberately bounded set of the highest-value existing actions
 * - facility verify/reject/suspend/reinstate ({@code FacilityService}),
 * doctor verify/reject ({@code DoctorService}), facility user create/
 * deactivate/reactivate ({@code FacilityUserService}), and erasure-
 * request resolution ({@code ErasureRequestService}) - not universally
 * wired into every write in the application via AOP/interceptor. Doing
 * that properly (deciding what "before/after" means for dozens of
 * unrelated entities) is a cross-cutting change disproportionate to one
 * build-order step; this gives Super Admin a real, populated audit
 * trail of the actions most likely to matter for platform oversight
 * without pretending to be exhaustive.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public void record(Long facilityId, Long actorUserId, String action, String entityType, Long entityId, String beforeData, String afterData) {
        AuditLogEntry entry = AuditLogEntry.builder()
                .facilityId(facilityId)
                .actorUserId(actorUserId)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .beforeData(truncate(beforeData))
                .afterData(truncate(afterData))
                .build();
        auditLogRepository.save(entry);
    }

    public Page<AuditLogEntry> search(Long facilityId, String action, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return auditLogRepository.search(facilityId, action, pageable);
    }

    /**
     * Facility-scoped read (Master Spec §17.7 #38) - unlike {@link #search},
     * the caller never supplies facilityId; it's always the current
     * request's own tenant, so a Hospital/Clinic Admin can only ever see
     * their own facility's slice, never another one's.
     */
    public Page<AuditLogEntry> searchForFacility(Long facilityId, int page, int size) {
        return auditLogRepository.search(facilityId, null, PageRequest.of(page, size));
    }

    private String truncate(String value) {
        if (value == null) return null;
        return value.length() > 2000 ? value.substring(0, 2000) : value;
    }
}
