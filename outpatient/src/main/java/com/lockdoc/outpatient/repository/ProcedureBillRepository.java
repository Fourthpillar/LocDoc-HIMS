package com.lockdoc.outpatient.repository;

import com.lockdoc.outpatient.entity.ProcedureBill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProcedureBillRepository extends JpaRepository<ProcedureBill, Long> {

    Optional<ProcedureBill> findByIdAndFacilityId(Long id, Long facilityId);

    List<ProcedureBill> findByFacilityIdOrderByCreatedDateDesc(Long facilityId);

    List<ProcedureBill> findByPatientIdOrderByCreatedDateDesc(Long patientId);
}
