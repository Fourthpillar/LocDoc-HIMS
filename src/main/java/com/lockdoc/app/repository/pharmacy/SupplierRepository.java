package com.lockdoc.app.repository.pharmacy;

import com.lockdoc.app.entity.pharmacy.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    Optional<Supplier> findByIdAndFacilityId(Long id, Long facilityId);

    boolean existsByFacilityIdAndNameIgnoreCase(Long facilityId, String name);

    Page<Supplier> findByFacilityIdAndActiveTrue(Long facilityId, Pageable pageable);

    @Query("SELECT s FROM Supplier s WHERE s.facility.id = :facilityId AND s.active = true AND "
            + "LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Supplier> search(@Param("facilityId") Long facilityId, @Param("search") String search, Pageable pageable);
}
