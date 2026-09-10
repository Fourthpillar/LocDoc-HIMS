package com.lockdoc.pharmacy.repository;

import com.lockdoc.pharmacy.entity.Medicine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    Optional<Medicine> findByCode(String code);

    boolean existsByCode(String code);

    Page<Medicine> findByActiveTrue(Pageable pageable);

    List<Medicine> findByActiveTrue();

    @Query("SELECT m FROM Medicine m WHERE m.active = true AND "
            + "(LOWER(m.name) LIKE LOWER(CONCAT('%', :search, '%')) "
            + "OR LOWER(m.code) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Medicine> search(@Param("search") String search, Pageable pageable);
}
