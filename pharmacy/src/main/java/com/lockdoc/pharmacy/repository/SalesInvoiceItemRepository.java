package com.lockdoc.pharmacy.repository;

import com.lockdoc.pharmacy.entity.SalesInvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface SalesInvoiceItemRepository extends JpaRepository<SalesInvoiceItem, Long> {

    List<SalesInvoiceItem> findBySalesInvoiceId(Long salesInvoiceId);

    @Query("SELECT sii.medicine.id, SUM(sii.qty), SUM(sii.rate * sii.qty), SUM(sii.discountAmount), SUM(sii.amount) "
            + "FROM SalesInvoiceItem sii JOIN sii.salesInvoice si "
            + "WHERE si.saleDate BETWEEN :from AND :to AND si.status = 'POSTED' "
            + "GROUP BY sii.medicine.id")
    List<Object[]> aggregateSalesByMedicine(@Param("from") LocalDate from, @Param("to") LocalDate to);
}
