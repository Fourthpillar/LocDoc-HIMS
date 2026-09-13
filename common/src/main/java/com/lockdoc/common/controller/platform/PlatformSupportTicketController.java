package com.lockdoc.common.controller.platform;

import com.lockdoc.common.dto.PageResponse;
import com.lockdoc.common.dto.platform.SupportTicketResponse;
import com.lockdoc.common.dto.platform.SupportTicketStatusRequest;
import com.lockdoc.common.service.platform.SupportTicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** Super Admin's triage queue (Master Spec §10, screen #42a) - cross-facility by design. */
@RestController
@RequestMapping("/platform/support-tickets")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('SUPPORT_TICKET_MANAGE')")
public class PlatformSupportTicketController {

    private final SupportTicketService supportTicketService;

    @GetMapping
    public ResponseEntity<PageResponse<SupportTicketResponse>> list(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(supportTicketService.allTickets(page, size));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<SupportTicketResponse> updateStatus(@PathVariable Long id, @Valid @RequestBody SupportTicketStatusRequest request) {
        return ResponseEntity.ok(supportTicketService.updateStatus(id, request.getStatus(), request.getResolutionNotes()));
    }
}
