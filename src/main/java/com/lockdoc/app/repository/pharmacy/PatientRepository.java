package com.lockdoc.app.repository.pharmacy;

import com.lockdoc.app.entity.pharmacy.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByIdAndFacilityId(Long id, Long facilityId);

    Optional<Patient> findByFacilityIdAndMrn(Long facilityId, String mrn);

    boolean existsByFacilityIdAndMrn(Long facilityId, String mrn);

    Page<Patient> findByFacilityIdAndActiveTrue(Long facilityId, Pageable pageable);

    /**
     * Name, UHID or mobile.
     *
     * Phone was missing, which made the front desk's own promise ("Search by name, UHID or
     * mobile") false: a patient who gave their number and nothing else could not be found,
     * and 7.4's duplicate detection - which is specified *on mobile* - had nothing to
     * detect with, so the same person could be issued a second UHID by simply being typed
     * in twice.
     */
    @Query("SELECT p FROM Patient p WHERE p.facility.id = :facilityId AND p.active = true AND "
            + "(LOWER(p.fullName) LIKE LOWER(CONCAT('%', :search, '%')) "
            + "OR LOWER(p.mrn) LIKE LOWER(CONCAT('%', :search, '%')) "
            + "OR p.phone LIKE CONCAT('%', :search, '%'))")
    Page<Patient> search(@Param("facilityId") Long facilityId, @Param("search") String search, Pageable pageable);
}
