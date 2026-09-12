package com.lockdoc.app.repository.op;

import com.lockdoc.app.entity.op.Package;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PackageRepository extends JpaRepository<Package, Long> {

    List<Package> findByFacilityIdAndActiveTrueOrderByNameAsc(Long facilityId);

    Optional<Package> findByIdAndFacilityId(Long id, Long facilityId);
}
