package com.lockdoc.outpatient.repository;

import com.lockdoc.outpatient.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByBillIdOrderByPaidAtAsc(Long billId);

    /** Counter-session reconciliation (§7.5, screens #12/#31) - CASH received at this facility within a shift window. */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.facility.id = :facilityId "
            + "AND p.paymentType = 'CASH' AND p.paidAt BETWEEN :from AND :to")
    BigDecimal sumCashInWindow(@Param("facilityId") Long facilityId, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /** Day/gross collection report (§17.7 #12a). */
    List<Payment> findByFacilityIdAndPaidAtBetweenOrderByPaidAtAsc(Long facilityId, LocalDateTime from, LocalDateTime to);
}
