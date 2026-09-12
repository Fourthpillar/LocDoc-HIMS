package com.lockdoc.app.repository.pharmacy;

import com.lockdoc.app.entity.pharmacy.SalesInvoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SalesInvoiceRepository extends JpaRepository<SalesInvoice, Long> {

    Optional<SalesInvoice> findByIdAndFacilityId(Long id, Long facilityId);

    boolean existsByFacilityIdAndInvoiceNumber(Long facilityId, String invoiceNumber);

    @Query("SELECT si FROM SalesInvoice si WHERE si.facility.id = :facilityId AND LOWER(si.invoiceNumber) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<SalesInvoice> search(@Param("facilityId") Long facilityId, @Param("search") String search, Pageable pageable);

    Page<SalesInvoice> findByFacilityId(Long facilityId, Pageable pageable);

    List<SalesInvoice> findByFacilityIdAndSaleDateBetweenAndStatus(Long facilityId, LocalDate from, LocalDate to, String status);

    /** Erasure eligibility (Master Spec §18.4) - a patient with any pharmacy sale history cannot be hard-deleted, only anonymized. */
    boolean existsByPatientId(Long patientId);

    /** Counter-session reconciliation (§11.5, screen #31) - CASH collected at this facility within a shift window, excluding cancelled sales. */
    @Query("SELECT COALESCE(SUM(si.amountPaid), 0) FROM SalesInvoice si WHERE si.facility.id = :facilityId "
            + "AND si.paymentMode = 'CASH' AND si.status <> 'CANCELLED' AND si.createdDate BETWEEN :from AND :to")
    BigDecimal sumCashInWindow(@Param("facilityId") Long facilityId, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /** OP → pharmacy conversion report (§17.7 #12a) - which of a given set of patients bought anything in the date range. */
    @Query("SELECT DISTINCT si.patient.id FROM SalesInvoice si WHERE si.facility.id = :facilityId "
            + "AND si.patient.id IN :patientIds AND si.saleDate BETWEEN :from AND :to AND si.status <> 'CANCELLED'")
    List<Long> findPatientIdsWithSaleInRange(@Param("facilityId") Long facilityId, @Param("patientIds") List<Long> patientIds,
                                              @Param("from") LocalDate from, @Param("to") LocalDate to);
}
