package com.lockdoc.outpatient.repository;

import com.lockdoc.outpatient.entity.Bill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BillRepository extends JpaRepository<Bill, Long> {

    Optional<Bill> findByIdAndFacilityId(Long id, Long facilityId);

    Optional<Bill> findByEncounterTypeAndEncounterId(String encounterType, Long encounterId);

    Page<Bill> findByFacilityIdAndStatusInOrderByCreatedDateDesc(Long facilityId, List<String> statuses, Pageable pageable);

    List<Bill> findByFacilityIdAndRefundStatusOrderByCreatedDateDesc(Long facilityId, String refundStatus);

    /** Every bill raised for one patient at this facility, paid or not - the Patient Record screen (§17.7 #3). */
    List<Bill> findByPatientIdAndFacilityIdOrderByCreatedDateDesc(Long patientId, Long facilityId);

    // LEFT JOIN, not an implicit b.patient.x inner join - a walk-in
    // procedure bill (nullable patient_id, V44) must still be findable by
    // bill number, and must never silently vanish from "every outstanding
    // bill" just because it has no patient to match by name/MRN/phone.
    @Query("SELECT b FROM Bill b LEFT JOIN b.patient p WHERE b.facility.id = :facilityId AND b.status IN ('UNPAID','PARTIALLY_PAID') "
            + "AND (LOWER(p.fullName) LIKE LOWER(CONCAT('%', :search, '%')) "
            + "OR LOWER(p.mrn) LIKE LOWER(CONCAT('%', :search, '%')) "
            + "OR LOWER(b.billNo) LIKE LOWER(CONCAT('%', :search, '%')) "
            + "OR p.phone LIKE CONCAT('%', :search, '%'))")
    List<Bill> searchDue(@Param("facilityId") Long facilityId, @Param("search") String search);
}
