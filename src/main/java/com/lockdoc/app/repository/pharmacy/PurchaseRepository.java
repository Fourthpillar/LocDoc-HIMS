package com.lockdoc.app.repository.pharmacy;

import com.lockdoc.app.entity.pharmacy.Purchase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    Optional<Purchase> findByIdAndFacilityId(Long id, Long facilityId);

    boolean existsByFacilityIdAndGrnNumber(Long facilityId, String grnNumber);

    boolean existsByFacilityIdAndPurchaseOrderId(Long facilityId, Long purchaseOrderId);

    @Query("SELECT p FROM Purchase p WHERE p.facility.id = :facilityId AND LOWER(p.grnNumber) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Purchase> search(@Param("facilityId") Long facilityId, @Param("search") String search, Pageable pageable);

    Page<Purchase> findByFacilityId(Long facilityId, Pageable pageable);

    List<Purchase> findByFacilityIdAndPurchaseDateBetweenAndStatus(Long facilityId, LocalDate from, LocalDate to, String status);

    List<Purchase> findByFacilityIdAndBalanceDueGreaterThanAndStatusOrderByDueDateAsc(Long facilityId, BigDecimal balanceDue, String status);

    /** Supplier ledger (§17.7 #32) - every GRN against this supplier, oldest first. */
    List<Purchase> findByFacilityIdAndSupplierIdOrderByPurchaseDateAsc(Long facilityId, Long supplierId);
}
