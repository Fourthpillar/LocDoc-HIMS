package com.lockdoc.app.repository.pharmacy;

import com.lockdoc.app.entity.pharmacy.PurchaseOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    Optional<PurchaseOrder> findByIdAndFacilityId(Long id, Long facilityId);

    boolean existsByFacilityIdAndPoNumber(Long facilityId, String poNumber);

    @Query("SELECT po FROM PurchaseOrder po WHERE po.facility.id = :facilityId AND LOWER(po.poNumber) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<PurchaseOrder> search(@Param("facilityId") Long facilityId, @Param("search") String search, Pageable pageable);

    Page<PurchaseOrder> findByFacilityId(Long facilityId, Pageable pageable);

    List<PurchaseOrder> findByFacilityIdAndOrderDateBetween(Long facilityId, LocalDate from, LocalDate to);
}
