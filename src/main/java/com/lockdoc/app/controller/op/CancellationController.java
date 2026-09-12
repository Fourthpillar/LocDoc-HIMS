package com.lockdoc.app.controller.op;

import com.lockdoc.app.dto.op.CancellationRequest;
import com.lockdoc.app.dto.op.CancellationResponse;
import com.lockdoc.app.service.op.CancellationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Master Spec §17.7 screen #9 (request side) / #35 (approve side, folded into step 8's unified queue later). */
@RestController
@RequestMapping("/op/cancellations")
@RequiredArgsConstructor
public class CancellationController {

    private final CancellationService cancellationService;

    @PostMapping("/{entityType}/{entityId}")
    @PreAuthorize("hasAuthority('OP_CANCELLATION_REQUEST')")
    public ResponseEntity<CancellationResponse> request(
            @PathVariable String entityType, @PathVariable Long entityId, @Valid @RequestBody CancellationRequest request) {
        return ResponseEntity.ok(cancellationService.request(entityType.toUpperCase(), entityId, request));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAuthority('OP_CANCELLATION_APPROVE')")
    public ResponseEntity<List<CancellationResponse>> pending() {
        return ResponseEntity.ok(cancellationService.pending());
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('OP_CANCELLATION_APPROVE')")
    public ResponseEntity<CancellationResponse> approve(@PathVariable Long id) {
        return ResponseEntity.ok(cancellationService.approve(id));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('OP_CANCELLATION_APPROVE')")
    public ResponseEntity<CancellationResponse> reject(@PathVariable Long id) {
        return ResponseEntity.ok(cancellationService.reject(id));
    }
}
