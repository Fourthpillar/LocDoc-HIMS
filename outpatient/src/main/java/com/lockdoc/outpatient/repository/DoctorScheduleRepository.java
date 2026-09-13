package com.lockdoc.outpatient.repository;

import com.lockdoc.outpatient.entity.DoctorSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DoctorScheduleRepository extends JpaRepository<DoctorSchedule, Long> {

    List<DoctorSchedule> findByFacilityIdAndActiveTrue(Long facilityId);

    List<DoctorSchedule> findByDoctorIdAndFacilityIdAndActiveTrue(Long doctorId, Long facilityId);

    /** All of a doctor's recurring sessions across every facility they're mapped to (Availability Planner, §8.2). */
    List<DoctorSchedule> findByDoctorIdAndActiveTrue(Long doctorId);

    Optional<DoctorSchedule> findByIdAndFacilityId(Long id, Long facilityId);

    Optional<DoctorSchedule> findByIdAndDoctorId(Long id, Long doctorId);
}
