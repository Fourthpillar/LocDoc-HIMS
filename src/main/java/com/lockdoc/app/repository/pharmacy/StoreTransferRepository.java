package com.lockdoc.app.repository.pharmacy;

import com.lockdoc.app.entity.pharmacy.StoreTransfer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoreTransferRepository extends JpaRepository<StoreTransfer, Long> {
    Page<StoreTransfer> findByFacilityId(Long facilityId, Pageable pageable);
    Page<StoreTransfer> findByFacilityIdAndToStoreIdAndStatus(Long facilityId, Long toStoreId, String status, Pageable pageable);
    Optional<StoreTransfer> findByIdAndFacilityId(Long id, Long facilityId);
}
