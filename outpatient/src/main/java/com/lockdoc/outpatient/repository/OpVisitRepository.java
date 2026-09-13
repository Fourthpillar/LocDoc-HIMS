package com.lockdoc.outpatient.repository;

import com.lockdoc.outpatient.entity.OpVisit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OpVisitRepository extends JpaRepository<OpVisit, Long> {

    Optional<OpVisit> findByIdAndFacilityId(Long id, Long facilityId);

    List<OpVisit> findByFacilityIdAndArrivedTsBetweenOrderByArrivedTsAsc(Long facilityId, LocalDateTime from, LocalDateTime to);

    List<OpVisit> findByDoctorIdAndArrivedTsBetweenOrderByArrivedTsAsc(Long doctorId, LocalDateTime from, LocalDateTime to);

    /** Erasure eligibility (Master Spec §18.4) - a patient with any OP visit history cannot be hard-deleted, only anonymized. */
    boolean existsByPatientId(Long patientId);

    /** MLC register (Master Spec §17.7 #38, §7.5) - "the flag alone isn't the deliverable, the register is". */
    List<OpVisit> findByFacilityIdAndMlcFlagTrueAndArrivedTsBetweenOrderByArrivedTsAsc(Long facilityId, LocalDateTime from, LocalDateTime to);

    /** Free-review eligibility (§7.4) - this patient-doctor pair's visit history, newest first, to find the most recent paid consultation. */
    List<OpVisit> findByPatientIdAndDoctorIdOrderByArrivedTsDesc(Long patientId, Long doctorId);

    /** One patient's visit history at this facility - the Patient Record screen (§17.7 #3). */
    List<OpVisit> findByPatientIdAndFacilityIdOrderByArrivedTsDesc(Long patientId, Long facilityId);
}
