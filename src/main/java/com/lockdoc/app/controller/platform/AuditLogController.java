package com.lockdoc.app.controller.platform;

import com.lockdoc.app.dto.PageResponse;
import com.lockdoc.app.dto.platform.AuditLogResponse;
import com.lockdoc.app.entity.platform.AuditLogEntry;
import com.lockdoc.app.service.platform.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** Cross-facility audit log (Master Spec §10, screen #42) - Super Admin, read-only. */
@RestController
@RequestMapping("/platform/audit-log")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('AUDIT_LOG_VIEW')")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<PageResponse<AuditLogResponse>> list(
            @RequestParam(required = false) Long facilityId,
            @RequestParam(required = false) String action,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<AuditLogEntry> result = auditLogService.search(facilityId, action, page, size);
        return ResponseEntity.ok(PageResponse.of(result, AuditLogResponse::toResponse));
    }
}
