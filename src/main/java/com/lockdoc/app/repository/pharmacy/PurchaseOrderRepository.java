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

    Optional<PurchaseOrder> findByPoNumber(String poNumber);

    boolean existsByPoNumber(String poNumber);

    @Query("SELECT po FROM PurchaseOrder po WHERE LOWER(po.poNumber) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<PurchaseOrder> search(@Param("search") String search, Pageable pageable);

    List<PurchaseOrder> findByOrderDateBetween(LocalDate from, LocalDate to);
}
