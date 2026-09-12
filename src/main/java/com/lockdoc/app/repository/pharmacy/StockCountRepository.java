package com.lockdoc.app.repository.pharmacy;

import com.lockdoc.app.entity.pharmacy.StockCount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StockCountRepository extends JpaRepository<StockCount, Long> {
    Page<StockCount> findByFacilityId(Long facilityId, Pageable pageable);
    Optional<StockCount> findByIdAndFacilityId(Long id, Long facilityId);
}
