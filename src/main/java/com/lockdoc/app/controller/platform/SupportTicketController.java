package com.lockdoc.app.controller.platform;

import com.lockdoc.app.dto.platform.SupportTicketRequest;
import com.lockdoc.app.dto.platform.SupportTicketResponse;
import com.lockdoc.app.service.platform.SupportTicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Raising side (Master Spec §10) - Hospital/Clinic Admin or Doctor. Super Admin's triage queue is {@link PlatformSupportTicketController}. */
@RestController
@RequestMapping("/facility/support-tickets")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('SUPPORT_TICKET_CREATE')")
public class SupportTicketController {

    private final SupportTicketService supportTicketService;

    @PostMapping
    public ResponseEntity<SupportTicketResponse> create(@Valid @RequestBody SupportTicketRequest request) {
        return ResponseEntity.ok(supportTicketService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<SupportTicketResponse>> mine() {
        return ResponseEntity.ok(supportTicketService.mine());
    }
}
