package com.lockdoc.app.service.pharmacy;

import com.lockdoc.app.config.SecurityUtils;
import com.lockdoc.app.dto.pharmacy.StoreRequest;
import com.lockdoc.app.dto.pharmacy.StoreResponse;
import com.lockdoc.app.entity.Facility;
import com.lockdoc.app.entity.Store;
import com.lockdoc.app.exception.DuplicateResourceException;
import com.lockdoc.app.exception.ResourceNotFoundException;
import com.lockdoc.app.repository.FacilityRepository;
import com.lockdoc.app.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Store management (Master Spec §6/§11.2) - genuinely needed as of build
 * order step 7 (indent/transfer are "meaningless with one store", per
 * StoreResolutionService's own pre-existing comment). Every facility
 * already has one store (V9's bootstrap "Main Store") - this is what
 * lets a facility add a second one and make those features real.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class StoreService {

    private final StoreRepository storeRepository;
    private final FacilityRepository facilityRepository;

    public List<StoreResponse> listForFacility() {
        Long facilityId = SecurityUtils.requireFacilityId();
        return storeRepository.findByFacilityIdAndActiveTrue(facilityId).stream().map(StoreResponse::toResponse).toList();
    }

    public StoreResponse create(StoreRequest request) {
        Long facilityId = SecurityUtils.requireFacilityId();
        if (storeRepository.existsByFacilityIdAndCodeIgnoreCase(facilityId, request.getCode())) {
            throw new DuplicateResourceException("A store already exists with code: " + request.getCode());
        }
        Facility facility = facilityRepository.findById(facilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Facility not found with id: " + facilityId));
        Store store = Store.builder().facility(facility).name(request.getName()).code(request.getCode()).active(true).build();
        return StoreResponse.toResponse(storeRepository.save(store));
    }
}
