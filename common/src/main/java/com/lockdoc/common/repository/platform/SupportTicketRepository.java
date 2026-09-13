package com.lockdoc.common.repository.platform;

import com.lockdoc.common.entity.platform.SupportTicket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {
    List<SupportTicket> findByFacilityIdOrderByCreatedDateDesc(Long facilityId);
    Page<SupportTicket> findAllByOrderByCreatedDateAsc(Pageable pageable);
    Optional<SupportTicket> findByIdAndFacilityId(Long id, Long facilityId);
}
