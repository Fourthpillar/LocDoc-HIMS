package com.lockdoc.app.service.op;

import com.lockdoc.app.config.SecurityUtils;
import com.lockdoc.app.dto.op.PackageItemRequest;
import com.lockdoc.app.dto.op.PackageItemResponse;
import com.lockdoc.app.dto.op.PackageRequest;
import com.lockdoc.app.dto.op.PackageResponse;
import com.lockdoc.app.entity.Facility;
import com.lockdoc.app.entity.op.BillableItem;
import com.lockdoc.app.entity.op.PackageItem;
import com.lockdoc.app.exception.ResourceNotFoundException;
import com.lockdoc.app.repository.FacilityRepository;
import com.lockdoc.app.repository.op.BillableItemRepository;
import com.lockdoc.app.repository.op.PackageItemRepository;
import com.lockdoc.app.repository.op.PackageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Package masters (Master Spec §6/§7.5, screen #11) - what a package contains; selling/drawdown is {@link PackageSaleService}. */
@Service
@RequiredArgsConstructor
@Transactional
public class PackageService {

    private final PackageRepository packageRepository;
    private final PackageItemRepository packageItemRepository;
    private final BillableItemRepository billableItemRepository;
    private final FacilityRepository facilityRepository;

    public List<PackageResponse> list() {
        Long facilityId = SecurityUtils.requireFacilityId();
        return packageRepository.findByFacilityIdAndActiveTrueOrderByNameAsc(facilityId).stream()
                .map(p -> PackageResponse.toResponse(p, itemsFor(p.getId())))
                .toList();
    }

    public PackageResponse create(PackageRequest request) {
        Long facilityId = SecurityUtils.requireFacilityId();
        Facility facility = facilityRepository.getReferenceById(facilityId);

        com.lockdoc.app.entity.op.Package pkg = com.lockdoc.app.entity.op.Package.builder()
                .facility(facility)
                .name(request.getName())
                .code(request.getCode())
                .price(request.getPrice())
                .active(true)
                .build();
        pkg = packageRepository.save(pkg);
        saveItems(pkg, request.getItems(), facilityId);

        return PackageResponse.toResponse(pkg, itemsFor(pkg.getId()));
    }

    public PackageResponse update(Long id, PackageRequest request) {
        Long facilityId = SecurityUtils.requireFacilityId();
        com.lockdoc.app.entity.op.Package pkg = packageRepository.findByIdAndFacilityId(id, facilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Package not found with id: " + id));
        pkg.setName(request.getName());
        pkg.setCode(request.getCode());
        pkg.setPrice(request.getPrice());
        pkg = packageRepository.save(pkg);

        // Replace the item list wholesale rather than diffing - simpler, and existing
        // PackageUtilization rows from past sales reference billable_items directly,
        // not package_items, so they're untouched by this (see V41's migration comment).
        packageItemRepository.deleteAll(packageItemRepository.findByPackageEntityId(pkg.getId()));
        saveItems(pkg, request.getItems(), facilityId);

        return PackageResponse.toResponse(pkg, itemsFor(pkg.getId()));
    }

    public void deactivate(Long id) {
        com.lockdoc.app.entity.op.Package pkg = packageRepository.findByIdAndFacilityId(id, SecurityUtils.requireFacilityId())
                .orElseThrow(() -> new ResourceNotFoundException("Package not found with id: " + id));
        pkg.setActive(false);
        packageRepository.save(pkg);
    }

    private void saveItems(com.lockdoc.app.entity.op.Package pkg, List<PackageItemRequest> items, Long facilityId) {
        for (PackageItemRequest itemRequest : items) {
            BillableItem billableItem = billableItemRepository.findByIdAndFacilityId(itemRequest.getBillableItemId(), facilityId)
                    .filter(BillableItem::getActive)
                    .orElseThrow(() -> new ResourceNotFoundException("Billable item not found with id: " + itemRequest.getBillableItemId()));
            packageItemRepository.save(PackageItem.builder()
                    .packageEntity(pkg)
                    .billableItem(billableItem)
                    .includedQty(itemRequest.getIncludedQty())
                    .build());
        }
    }

    private List<PackageItemResponse> itemsFor(Long packageId) {
        return packageItemRepository.findByPackageEntityId(packageId).stream().map(PackageItemResponse::toResponse).toList();
    }
}
