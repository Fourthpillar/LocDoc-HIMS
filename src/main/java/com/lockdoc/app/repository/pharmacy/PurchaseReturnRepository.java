package com.lockdoc.app.repository.pharmacy;

import com.lockdoc.app.entity.pharmacy.PurchaseReturn;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PurchaseReturnRepository extends JpaRepository<PurchaseReturn, Long> {
    Page<PurchaseReturn> findByFacilityId(Long facilityId, Pageable pageable);
    Optional<PurchaseReturn> findByIdAndFacilityId(Long id, Long facilityId);

    /** Supplier ledger (§17.7 #32) - every debit note against this supplier, newest first. */
    @Query("SELECT pr FROM PurchaseReturn pr WHERE pr.supplier.id = :supplierId AND pr.facility.id = :facilityId ORDER BY pr.createdDate ASC")
    List<PurchaseReturn> findBySupplierForLedger(@Param("facilityId") Long facilityId, @Param("supplierId") Long supplierId);
}
