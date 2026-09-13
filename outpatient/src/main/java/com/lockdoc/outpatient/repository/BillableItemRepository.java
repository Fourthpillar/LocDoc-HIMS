package com.lockdoc.outpatient.repository;

import com.lockdoc.outpatient.entity.BillableItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BillableItemRepository extends JpaRepository<BillableItem, Long> {

    List<BillableItem> findByFacilityIdAndItemTypeAndActiveTrueOrderByNameAsc(Long facilityId, String itemType);

    Optional<BillableItem> findByIdAndFacilityId(Long id, Long facilityId);
}
