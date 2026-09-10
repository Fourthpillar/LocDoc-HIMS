package com.lockdoc.pharmacy.repository;

import com.lockdoc.pharmacy.entity.SalesInvoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SalesInvoiceRepository extends JpaRepository<SalesInvoice, Long> {

    Optional<SalesInvoice> findByInvoiceNumber(String invoiceNumber);

    boolean existsByInvoiceNumber(String invoiceNumber);

    @Query("SELECT si FROM SalesInvoice si WHERE LOWER(si.invoiceNumber) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<SalesInvoice> search(@Param("search") String search, Pageable pageable);

    List<SalesInvoice> findBySaleDateBetweenAndStatus(LocalDate from, LocalDate to, String status);
}
