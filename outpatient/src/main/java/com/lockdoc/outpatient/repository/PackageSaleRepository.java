package com.lockdoc.outpatient.repository;

import com.lockdoc.outpatient.entity.PackageSale;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PackageSaleRepository extends JpaRepository<PackageSale, Long> {

    Optional<PackageSale> findByIdAndFacilityId(Long id, Long facilityId);

    List<PackageSale> findByPatientIdOrderByCreatedDateDesc(Long patientId);
}
