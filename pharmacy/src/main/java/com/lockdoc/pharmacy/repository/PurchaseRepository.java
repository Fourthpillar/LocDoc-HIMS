package com.lockdoc.pharmacy.repository;

import com.lockdoc.pharmacy.entity.Purchase;
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

    Optional<Purchase> findByGrnNumber(String grnNumber);

    boolean existsByGrnNumber(String grnNumber);

    boolean existsByPurchaseOrderId(Long purchaseOrderId);

    @Query("SELECT p FROM Purchase p WHERE LOWER(p.grnNumber) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Purchase> search(@Param("search") String search, Pageable pageable);

    List<Purchase> findByPurchaseDateBetweenAndStatus(LocalDate from, LocalDate to, String status);

    List<Purchase> findByBalanceDueGreaterThanAndStatusOrderByDueDateAsc(BigDecimal balanceDue, String status);
}
