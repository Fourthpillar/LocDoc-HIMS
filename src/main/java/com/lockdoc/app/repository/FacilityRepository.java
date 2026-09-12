package com.lockdoc.app.repository;

import com.lockdoc.app.entity.Facility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FacilityRepository extends JpaRepository<Facility, Long> {

    Optional<Facility> findByNameIgnoreCase(String name);

    Page<Facility> findByVerificationStatus(String verificationStatus, Pageable pageable);

    @Query("SELECT f FROM Facility f WHERE LOWER(f.name) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Facility> search(@Param("search") String search, Pageable pageable);
}
