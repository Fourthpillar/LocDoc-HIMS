package com.lockdoc.app.service.op;

import com.lockdoc.app.config.SecurityUtils;
import com.lockdoc.app.dto.op.BillableItemRequest;
import com.lockdoc.app.dto.op.BillableItemResponse;
import com.lockdoc.app.entity.Facility;
import com.lockdoc.app.entity.op.BillableItem;
import com.lockdoc.app.exception.InvalidDocumentStateException;
import com.lockdoc.app.exception.ResourceNotFoundException;
import com.lockdoc.app.repository.FacilityRepository;
import com.lockdoc.app.repository.op.BillableItemRepository;
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
