package com.lockdoc.app.repository.pharmacy;

import com.lockdoc.app.entity.pharmacy.Medicine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    // Tenant-safe single lookup - returns empty (never the row) if the id
    // exists but belongs to a different facility, so a cross-tenant access
    // attempt 404s rather than leaking that the id exists at all.
    Optional<Medicine> findByIdAndFacilityId(Long id, Long facilityId);

    Optional<Medicine> findByFacilityIdAndCode(Long facilityId, String code);

    boolean existsByFacilityIdAndCode(Long facilityId, String code);

    Page<Medicine> findByFacilityIdAndActiveTrue(Long facilityId, Pageable pageable);

    List<Medicine> findByFacilityIdAndActiveTrue(Long facilityId);

    @Query("SELECT m FROM Medicine m WHERE m.facility.id = :facilityId AND m.active = true AND "
            + "(LOWER(m.name) LIKE LOWER(CONCAT('%', :search, '%')) "
            + "OR LOWER(m.code) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Medicine> search(@Param("facilityId") Long facilityId, @Param("search") String search, Pageable pageable);

    /** Doctor Module's stock-availability lookup (§8.7) and Pharmacy's counter-sale search - by name or generic name. */
    @Query("SELECT m FROM Medicine m WHERE m.facility.id = :facilityId AND m.active = true AND "
            + "(LOWER(m.name) LIKE LOWER(CONCAT('%', :search, '%')) "
            + "OR LOWER(m.genericName) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Medicine> searchByNameOrGeneric(@Param("facilityId") Long facilityId, @Param("search") String search);

    /** Generic substitution (§11.5) - other active medicines in the same facility sharing a generic/molecule name. */
    List<Medicine> findByFacilityIdAndGenericNameIgnoreCaseAndActiveTrueAndIdNot(Long facilityId, String genericName, Long excludeId);
}
