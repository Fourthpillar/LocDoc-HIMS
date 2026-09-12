package com.lockdoc.app.repository.pharmacy;

import com.lockdoc.app.entity.pharmacy.MedicineBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MedicineBatchRepository extends JpaRepository<MedicineBatch, Long> {

    // Batch numbers are unique per store, not per facility - see V9 (the
    // same batch number can legitimately land in two different stores).
    Optional<MedicineBatch> findByStoreIdAndMedicineIdAndBatchNo(Long storeId, Long medicineId, String batchNo);

    List<MedicineBatch> findByStoreIdAndMedicineIdAndQuantityOnHandGreaterThanOrderByExpiryDateAsc(
            Long storeId, Long medicineId, Integer quantityOnHand);

    List<MedicineBatch> findByFacilityIdAndQuantityOnHandGreaterThanOrderByMedicine_NameAsc(Long facilityId, Integer quantityOnHand);

    /** Physical stock count's snapshot source (§11.4) - every batch actually on hand at one store. */
    List<MedicineBatch> findByStoreIdAndQuantityOnHandGreaterThanOrderByMedicine_NameAsc(Long storeId, Integer quantityOnHand);

    /**
     * Non-expired stock aggregated per medicine, used by InventoryService/ReportService
     * to build the stock summary and low-stock reports - scoped to one facility.
     */
    @Query("SELECT mb.medicine.id, SUM(mb.quantityOnHand) FROM MedicineBatch mb "
            + "WHERE mb.facility.id = :facilityId AND mb.expiryDate >= CURRENT_DATE GROUP BY mb.medicine.id")
    List<Object[]> aggregateActiveStockByMedicine(@Param("facilityId") Long facilityId);

    @Query("SELECT mb FROM MedicineBatch mb WHERE mb.facility.id = :facilityId "
            + "AND mb.expiryDate <= :beforeDate AND mb.quantityOnHand > 0 ORDER BY mb.expiryDate ASC")
    List<MedicineBatch> findExpiringBatches(@Param("facilityId") Long facilityId, @Param("beforeDate") java.time.LocalDate beforeDate);
}
