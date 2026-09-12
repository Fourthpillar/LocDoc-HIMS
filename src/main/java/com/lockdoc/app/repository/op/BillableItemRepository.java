package com.lockdoc.app.repository.op;

import com.lockdoc.app.entity.op.BillableItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BillableItemRepository extends JpaRepository<BillableItem, Long> {

    List<BillableItem> findByFacilityIdAndItemTypeAndActiveTrueOrderByNameAsc(Long facilityId, String itemType);

    Optional<BillableItem> findByIdAndFacilityId(Long id, Long facilityId);
}
