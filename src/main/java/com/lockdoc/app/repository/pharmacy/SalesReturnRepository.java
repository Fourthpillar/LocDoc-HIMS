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

    Optional<SalesReturn> findByIdAndFacilityId(Long id, Long facilityId);

    boolean existsByFacilityIdAndReturnNumber(Long facilityId, String returnNumber);

    boolean existsBySalesInvoiceId(Long salesInvoiceId);

    @Query("SELECT sr FROM SalesReturn sr WHERE sr.facility.id = :facilityId AND LOWER(sr.returnNumber) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<SalesReturn> search(@Param("facilityId") Long facilityId, @Param("search") String search, Pageable pageable);

    Page<SalesReturn> findByFacilityId(Long facilityId, Pageable pageable);

    List<SalesReturn> findByFacilityIdAndReturnDateBetweenAndStatus(Long facilityId, LocalDate from, LocalDate to, String status);
}
