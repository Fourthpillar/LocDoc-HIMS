package com.lockdoc.app.repository.op;

import com.lockdoc.app.entity.op.Pro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProRepository extends JpaRepository<Pro, Long> {
    List<Pro> findByFacilityIdAndActiveTrueOrderByNameAsc(Long facilityId);
    Optional<Pro> findByIdAndFacilityId(Long id, Long facilityId);
}
