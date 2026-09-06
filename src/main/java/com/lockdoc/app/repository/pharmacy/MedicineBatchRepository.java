package com.lockdoc.app.repository.pharmacy;

import com.lockdoc.app.entity.pharmacy.MedicineBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MedicineBatchRepository extends JpaRepository<MedicineBatch, Long> {

    Optional<MedicineBatch> findByMedicineIdAndBatchNo(Long medicineId, String batchNo);

    List<MedicineBatch> findByMedicineIdAndQuantityOnHandGreaterThanOrderByExpiryDateAsc(Long medicineId, Integer quantityOnHand);

    List<MedicineBatch> findByQuantityOnHandGreaterThanOrderByMedicine_NameAsc(Integer quantityOnHand);

    /**
     * Non-expired stock aggregated per medicine, used by InventoryService/ReportService
     * to build the stock summary and low-stock reports.
     */
    @Query("SELECT mb.medicine.id, SUM(mb.quantityOnHand) FROM MedicineBatch mb "
            + "WHERE mb.expiryDate >= CURRENT_DATE GROUP BY mb.medicine.id")
    List<Object[]> aggregateActiveStockByMedicine();

    @Query("SELECT mb FROM MedicineBatch mb WHERE mb.expiryDate <= :beforeDate AND mb.quantityOnHand > 0 "
            + "ORDER BY mb.expiryDate ASC")
    List<MedicineBatch> findExpiringBatches(@Param("beforeDate") java.time.LocalDate beforeDate);
}
