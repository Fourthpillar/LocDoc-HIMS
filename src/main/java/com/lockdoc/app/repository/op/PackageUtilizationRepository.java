package com.lockdoc.app.repository.op;

import com.lockdoc.app.entity.op.PackageUtilization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PackageUtilizationRepository extends JpaRepository<PackageUtilization, Long> {

    List<PackageUtilization> findByPackageSaleId(Long packageSaleId);

    Optional<PackageUtilization> findByIdAndPackageSaleFacilityId(Long id, Long facilityId);
}
