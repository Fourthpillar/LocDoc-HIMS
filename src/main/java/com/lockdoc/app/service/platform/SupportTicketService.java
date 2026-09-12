package com.lockdoc.app.service.platform;

import com.lockdoc.app.config.SecurityUtils;
import com.lockdoc.app.dto.PageResponse;
import com.lockdoc.app.dto.platform.SupportTicketRequest;
import com.lockdoc.app.dto.platform.SupportTicketResponse;
import com.lockdoc.app.entity.Facility;
import com.lockdoc.app.entity.platform.SupportTicket;
import com.lockdoc.app.exception.InvalidDocumentStateException;
import com.lockdoc.app.exception.ResourceNotFoundException;
import com.lockdoc.app.repository.FacilityRepository;
import com.lockdoc.app.repository.platform.SupportTicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Support & dispute tickets (Master Spec §10, screen #42a) - "a
 * facility, doctor, or Hospital/Clinic Admin raises an issue, Super
 * Admin triages and resolves it." A Doctor has no single home facility
 * of their own (they may practise at several, same as Super Admin at
 * the principal level) - {@code request.facilityId} covers that case;
 * a Hospital/Clinic Admin's own facility is always used regardless of
 * what the request carries, so they can never raise a ticket
 * attributed to a facility they don't belong to.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class SupportTicketService {

    private final SupportTicketRepository supportTicketRepository;
    private final FacilityRepository facilityRepository;

    public SupportTicketResponse create(SupportTicketRequest request) {
        Long principalFacilityId = SecurityUtils.currentFacilityId();
        Long facilityId = principalFacilityId != null ? principalFacilityId : request.getFacilityId();
        if (facilityId == null) {
            throw new InvalidDocumentStateException("facilityId is required when raising a ticket without a single home facility");
        }
        Facility facility = facilityRepository.findById(facilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Facility not found with id: " + facilityId));

        SupportTicket ticket = SupportTicket.builder()
                .facility(facility)
                .raisedByUserId(SecurityUtils.currentUserId())
                .category(request.getCategory())
                .subject(request.getSubject())
                .description(request.getDescription())
                .status(SupportTicket.STATUS_OPEN)
                .build();
        return SupportTicketResponse.toResponse(supportTicketRepository.save(ticket));
    }

    /** Own facility's tickets, for the raiser to track their own submissions. */
    public List<SupportTicketResponse> mine() {
        Long facilityId = SecurityUtils.requireFacilityId();
        return supportTicketRepository.findByFacilityIdOrderByCreatedDateDesc(facilityId).stream()
                .map(SupportTicketResponse::toResponse)
                .toList();
    }

    /** Super Admin's cross-facility triage queue - oldest first, matching the Approval Queue's own convention (§17.7 #35). */
    public PageResponse<SupportTicketResponse> allTickets(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SupportTicket> result = supportTicketRepository.findAllByOrderByCreatedDateAsc(pageable);
        return PageResponse.of(result, SupportTicketResponse::toResponse);
    }

    public SupportTicketResponse updateStatus(Long id, String status, String resolutionNotes) {
        SupportTicket ticket = supportTicketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + id));
        ticket.setStatus(status);
        ticket.setResolutionNotes(resolutionNotes);
        if (SupportTicket.STATUS_RESOLVED.equals(status) || SupportTicket.STATUS_CLOSED.equals(status)) {
            ticket.setResolvedByUserId(SecurityUtils.currentUserId());
            ticket.setResolvedAt(LocalDateTime.now());
        }
        return SupportTicketResponse.toResponse(supportTicketRepository.save(ticket));
    }
}
