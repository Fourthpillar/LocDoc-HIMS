package com.lockdoc.app.controller.dataprotection;

import com.lockdoc.app.dto.dataprotection.ErasureRequestCreateRequest;
import com.lockdoc.app.dto.dataprotection.ErasureRequestResolveRequest;
import com.lockdoc.app.dto.dataprotection.ErasureRequestResponse;
import com.lockdoc.app.service.dataprotection.ErasureRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Master Spec §18.4/§18.6, screen #38b - own facility only. */
@RestController
@RequestMapping("/facility/erasure-requests")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('DATA_PROTECTION_ERASURE_MANAGE')")
public class ErasureRequestController {

    private final ErasureRequestService erasureRequestService;

    @GetMapping
    public ResponseEntity<List<ErasureRequestResponse>> list() {
        return ResponseEntity.ok(erasureRequestService.list());
    }

    @PostMapping
    public ResponseEntity<ErasureRequestResponse> create(@Valid @RequestBody ErasureRequestCreateRequest request) {
        return ResponseEntity.ok(erasureRequestService.create(request));
    }

    @PostMapping("/{id}/resolve")
    public ResponseEntity<ErasureRequestResponse> resolve(@PathVariable Long id, @Valid @RequestBody ErasureRequestResolveRequest request) {
        return ResponseEntity.ok(erasureRequestService.resolve(id, request));
    }
}
