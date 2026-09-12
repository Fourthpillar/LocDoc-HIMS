package com.lockdoc.app.repository.pharmacy;

import com.lockdoc.app.entity.pharmacy.Indent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IndentRepository extends JpaRepository<Indent, Long> {
    Page<Indent> findByFacilityId(Long facilityId, Pageable pageable);
    Page<Indent> findByFacilityIdAndStatus(Long facilityId, String status, Pageable pageable);
    Optional<Indent> findByIdAndFacilityId(Long id, Long facilityId);
    boolean existsByFacilityIdAndIndentNo(Long facilityId, String indentNo);
}
