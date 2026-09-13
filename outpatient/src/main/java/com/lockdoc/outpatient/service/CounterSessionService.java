package com.lockdoc.outpatient.service;

import com.lockdoc.common.config.SecurityUtils;
import com.lockdoc.outpatient.dto.CounterSessionCloseRequest;
import com.lockdoc.outpatient.dto.CounterSessionResponse;
import com.lockdoc.common.entity.Facility;
import com.lockdoc.outpatient.entity.CounterSession;
import com.lockdoc.common.exception.InvalidDocumentStateException;
import com.lockdoc.common.exception.ResourceNotFoundException;
import com.lockdoc.common.repository.FacilityRepository;
import com.lockdoc.outpatient.repository.CounterSessionRepository;
import com.lockdoc.outpatient.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * OP counter session open/close & cash reconciliation (Master Spec §7.5/
 * §11.5, screens #12/#31).
 *
 * systemTotal is computed at close time, not accumulated live - see
 * V37's migration comment for why. This means a payment posted after
 * close() runs but logically within the shift window (a race, not a
 * normal case) won't be swept in; acceptable for a reconciliation
 * screen where the numbers are being read back to a human at the
 * counter, not for a downstream automated settlement.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CounterSessionService {

    private final CounterSessionRepository counterSessionRepository;
    private final FacilityRepository facilityRepository;
    private final PaymentRepository paymentRepository;

    public CounterSessionResponse open(String counterType) {
        Long facilityId = SecurityUtils.requireFacilityId();
        validateCounterType(counterType);

        counterSessionRepository.findByFacilityIdAndCounterTypeAndStatus(facilityId, counterType, CounterSession.STATUS_OPEN)
                .ifPresent(existing -> {
                    throw new InvalidDocumentStateException("A " + counterType + " counter session is already open (id " + existing.getId() + ") - close it before opening a new one");
                });

        Facility facility = facilityRepository.getReferenceById(facilityId);
        CounterSession session = CounterSession.builder()
                .facility(facility)
                .counterType(counterType)
                .status(CounterSession.STATUS_OPEN)
                .openedByUserId(SecurityUtils.currentUserId())
                .openedAt(LocalDateTime.now())
                .build();
        return CounterSessionResponse.toResponse(counterSessionRepository.save(session));
    }

    public CounterSessionResponse current(String counterType) {
        Long facilityId = SecurityUtils.requireFacilityId();
        validateCounterType(counterType);
        return counterSessionRepository.findByFacilityIdAndCounterTypeAndStatus(facilityId, counterType, CounterSession.STATUS_OPEN)
                .map(CounterSessionResponse::toResponse)
                .orElse(null);
    }

    public List<CounterSessionResponse> history(String counterType) {
        Long facilityId = SecurityUtils.requireFacilityId();
        validateCounterType(counterType);
        return counterSessionRepository.findByFacilityIdAndCounterTypeOrderByOpenedAtDesc(facilityId, counterType).stream()
                .map(CounterSessionResponse::toResponse).toList();
    }

    public CounterSessionResponse close(Long id, CounterSessionCloseRequest request) {
        Long facilityId = SecurityUtils.requireFacilityId();
        CounterSession session = counterSessionRepository.findByIdAndFacilityId(id, facilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Counter session not found with id: " + id));
        if (!CounterSession.STATUS_OPEN.equals(session.getStatus())) {
            throw new InvalidDocumentStateException("Counter session can only be closed from OPEN status, current status: " + session.getStatus());
        }

        LocalDateTime closedAt = LocalDateTime.now();
        BigDecimal systemTotal = paymentRepository.sumCashInWindow(facilityId, session.getOpenedAt(), closedAt);
        systemTotal = systemTotal == null ? BigDecimal.ZERO : systemTotal;

        session.setStatus(CounterSession.STATUS_CLOSED);
        session.setClosedByUserId(SecurityUtils.currentUserId());
        session.setClosedAt(closedAt);
        session.setDeclaredCash(request.getDeclaredCash());
        session.setSystemTotal(systemTotal);
        session.setVariance(request.getDeclaredCash().subtract(systemTotal));

        return CounterSessionResponse.toResponse(counterSessionRepository.save(session));
    }

    private void validateCounterType(String counterType) {
        if (!CounterSession.TYPE_OP.equals(counterType)) {
            throw new InvalidDocumentStateException("counterType must be OP");
        }
    }
}
