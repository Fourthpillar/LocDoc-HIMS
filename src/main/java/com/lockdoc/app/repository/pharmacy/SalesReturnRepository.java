package com.lockdoc.app.repository.pharmacy;

import com.lockdoc.app.entity.pharmacy.SalesReturn;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SalesReturnRepository extends JpaRepository<SalesReturn, Long> {

    Optional<SalesReturn> findByReturnNumber(String returnNumber);

    boolean existsByReturnNumber(String returnNumber);

    boolean existsBySalesInvoiceId(Long salesInvoiceId);

    @Query("SELECT sr FROM SalesReturn sr WHERE LOWER(sr.returnNumber) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<SalesReturn> search(@Param("search") String search, Pageable pageable);

    List<SalesReturn> findByReturnDateBetweenAndStatus(LocalDate from, LocalDate to, String status);
}
