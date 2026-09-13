package com.lockdoc.outpatient.repository;

import com.lockdoc.outpatient.entity.Area;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AreaRepository extends JpaRepository<Area, Long> {
    List<Area> findByFacilityIdAndActiveTrueOrderByAreaNameAsc(Long facilityId);
    Optional<Area> findByIdAndFacilityId(Long id, Long facilityId);
}
