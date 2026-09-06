package com.lockdoc.app.repository.pharmacy;

import com.lockdoc.app.entity.pharmacy.SalesReturnItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface SalesReturnItemRepository extends JpaRepository<SalesReturnItem, Long> {

    List<SalesReturnItem> findBySalesInvoiceItemId(Long salesInvoiceItemId);

    List<SalesReturnItem> findBySalesReturnId(Long salesReturnId);

    @Query("SELECT COALESCE(SUM(sri.qty), 0) FROM SalesReturnItem sri WHERE sri.salesInvoiceItem.id = :salesInvoiceItemId")
    Integer sumReturnedQtyForInvoiceItem(@Param("salesInvoiceItemId") Long salesInvoiceItemId);

    @Query("SELECT sri.medicine.id, SUM(sri.qty), SUM(sri.amount) "
            + "FROM SalesReturnItem sri JOIN sri.salesReturn sr "
            + "WHERE sr.returnDate BETWEEN :from AND :to AND sr.status = 'POSTED' "
            + "GROUP BY sri.medicine.id")
    List<Object[]> aggregateReturnsByMedicine(@Param("from") LocalDate from, @Param("to") LocalDate to);
}
