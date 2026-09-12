package com.lockdoc.app.service.pharmacy;

import com.lockdoc.app.entity.Store;
import com.lockdoc.app.exception.ResourceNotFoundException;
import com.lockdoc.app.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Resolves the store a facility-scoped stock operation applies to.
 *
 * Every stock-bearing table is genuinely store-scoped now (Master Spec §6),
 * but there is no store-selection screen anywhere yet - that lands with
 * the multi-store Pharmacy-completion features (build order step 7). Until
 * then, a facility's first active store stands in as the implicit default,
 * so a single-store facility (every facility onboarded so far) never has
 * to think about stores at all - the same "a pure walk-in day must work
 * perfectly" graceful-degradation the OP module applies to appointments.
 */
@Service
@RequiredArgsConstructor
public class StoreResolutionService {

    private final StoreRepository storeRepository;

    public Store resolveDefaultStore(Long facilityId) {
        return storeRepository.findFirstByFacilityIdAndActiveTrueOrderByIdAsc(facilityId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Facility " + facilityId + " has no active store - onboarding is incomplete"));
    }
}
