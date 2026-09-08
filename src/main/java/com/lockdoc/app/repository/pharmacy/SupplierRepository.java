package com.lockdoc.app.repository.pharmacy;

import com.lockdoc.app.entity.pharmacy.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    boolean existsByNameIgnoreCase(String name);

    Page<Supplier> findByActiveTrue(Pageable pageable);

    @Query("SELECT s FROM Supplier s WHERE s.active = true AND "
            + "LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Supplier> search(@Param("search") String search, Pageable pageable);
}
