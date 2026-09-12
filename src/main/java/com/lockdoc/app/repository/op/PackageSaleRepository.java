package com.lockdoc.app.repository.op;

import com.lockdoc.app.entity.op.PackageSale;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PackageSaleRepository extends JpaRepository<PackageSale, Long> {

    Optional<PackageSale> findByIdAndFacilityId(Long id, Long facilityId);

    List<PackageSale> findByPatientIdOrderByCreatedDateDesc(Long patientId);
}
