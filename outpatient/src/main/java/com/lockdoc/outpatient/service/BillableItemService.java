package com.lockdoc.outpatient.service;

import com.lockdoc.common.config.SecurityUtils;
import com.lockdoc.outpatient.dto.BillableItemRequest;
import com.lockdoc.outpatient.dto.BillableItemResponse;
import com.lockdoc.common.entity.Facility;
import com.lockdoc.outpatient.entity.BillableItem;
import com.lockdoc.common.exception.InvalidDocumentStateException;
import com.lockdoc.common.exception.ResourceNotFoundException;
import com.lockdoc.common.repository.FacilityRepository;
import com.lockdoc.outpatient.repository.BillableItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Procedure/Service charge masters (Master Spec §7.2) - see V39's migration comment for why one table serves both. */
@Service
@RequiredArgsConstructor
@Transactional
public class BillableItemService {

    private static final List<String> VALID_TYPES = List.of(BillableItem.TYPE_SERVICE, BillableItem.TYPE_PROCEDURE);

    private final BillableItemRepository billableItemRepository;
    private final FacilityRepository facilityRepository;

    public List<BillableItemResponse> list(String itemType) {
        Long facilityId = SecurityUtils.requireFacilityId();
        validateType(itemType);
        return billableItemRepository.findByFacilityIdAndItemTypeAndActiveTrueOrderByNameAsc(facilityId, itemType).stream()
                .map(BillableItemResponse::toResponse).toList();
    }

    public BillableItemResponse create(BillableItemRequest request) {
        validateType(request.getItemType());
        Facility facility = facilityRepository.getReferenceById(SecurityUtils.requireFacilityId());
        BillableItem entity = BillableItem.builder()
                .facility(facility)
                .itemType(request.getItemType())
                .name(request.getName())
                .code(request.getCode())
                .rateDirect(request.getRateDirect())
                .rateOrganization(request.getRateOrganization())
                .rateTpa(request.getRateTpa())
                .active(true)
                .build();
        return BillableItemResponse.toResponse(billableItemRepository.save(entity));
    }

    public BillableItemResponse update(Long id, BillableItemRequest request) {
        validateType(request.getItemType());
        BillableItem entity = billableItemRepository.findByIdAndFacilityId(id, SecurityUtils.requireFacilityId())
                .orElseThrow(() -> new ResourceNotFoundException("Billable item not found with id: " + id));
        entity.setItemType(request.getItemType());
        entity.setName(request.getName());
        entity.setCode(request.getCode());
        entity.setRateDirect(request.getRateDirect());
        entity.setRateOrganization(request.getRateOrganization());
        entity.setRateTpa(request.getRateTpa());
        return BillableItemResponse.toResponse(billableItemRepository.save(entity));
    }

    public void deactivate(Long id) {
        BillableItem entity = billableItemRepository.findByIdAndFacilityId(id, SecurityUtils.requireFacilityId())
                .orElseThrow(() -> new ResourceNotFoundException("Billable item not found with id: " + id));
        entity.setActive(false);
        billableItemRepository.save(entity);
    }

    private void validateType(String itemType) {
        if (!VALID_TYPES.contains(itemType)) {
            throw new InvalidDocumentStateException("Unknown item type: " + itemType + " - expected one of " + VALID_TYPES);
        }
    }
}
