package com.lockdoc.app.repository;

import com.lockdoc.app.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store, Long> {

    List<Store> findByFacilityIdAndActiveTrue(Long facilityId);

    // Used to resolve an implicit store for facilities that have never
    // needed to choose one - see StoreResolutionService.
    Optional<Store> findFirstByFacilityIdAndActiveTrueOrderByIdAsc(Long facilityId);

    boolean existsByFacilityIdAndCodeIgnoreCase(Long facilityId, String code);
}
