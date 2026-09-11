package com.lockdoc.pharmacy.repository;

import com.lockdoc.pharmacy.entity.PurchaseItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseItemRepository extends JpaRepository<PurchaseItem, Long> {

    List<PurchaseItem> findByPurchaseId(Long purchaseId);
}
