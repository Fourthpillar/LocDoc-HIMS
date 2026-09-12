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

    @Query("SELECT p FROM Patient p WHERE p.facility.id = :facilityId AND p.active = true AND "
            + "(LOWER(p.fullName) LIKE LOWER(CONCAT('%', :search, '%')) "
            + "OR LOWER(p.mrn) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Patient> search(@Param("facilityId") Long facilityId, @Param("search") String search, Pageable pageable);
}
