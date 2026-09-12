package com.lockdoc.app.repository.pharmacy;

import com.lockdoc.app.entity.pharmacy.PurchaseReturnItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PurchaseReturnItemRepository extends JpaRepository<PurchaseReturnItem, Long> {

    /** Sums what's already been returned against one GRN line - the "capped at what remains un-returned" guard (§11.5). */
    @Query("SELECT COALESCE(SUM(pri.returnedQty), 0) FROM PurchaseReturnItem pri "
            + "WHERE pri.purchaseItem.id = :purchaseItemId AND pri.purchaseReturn.status = 'POSTED'")
    Integer sumReturnedQtyForPurchaseItem(@Param("purchaseItemId") Long purchaseItemId);

    List<PurchaseReturnItem> findByPurchaseReturnId(Long purchaseReturnId);
}
