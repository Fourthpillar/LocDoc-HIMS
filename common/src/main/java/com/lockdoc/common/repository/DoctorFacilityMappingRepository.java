package com.lockdoc.common.repository;

import com.lockdoc.common.entity.DoctorFacilityMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DoctorFacilityMappingRepository extends JpaRepository<DoctorFacilityMapping, Long> {

    List<DoctorFacilityMapping> findByDoctorIdOrderByRequestedAtDesc(Long doctorId);

    List<DoctorFacilityMapping> findByFacilityIdOrderByRequestedAtDesc(Long facilityId);

    Optional<DoctorFacilityMapping> findByIdAndDoctorId(Long id, Long doctorId);

    Optional<DoctorFacilityMapping> findByIdAndFacilityId(Long id, Long facilityId);

    List<DoctorFacilityMapping> findByDoctorIdAndFacilityIdAndStatusIn(Long doctorId, Long facilityId, List<String> statuses);

    List<DoctorFacilityMapping> findByDoctorIdAndStatus(Long doctorId, String status);

    List<DoctorFacilityMapping> findByFacilityIdAndStatus(Long facilityId, String status);
}
