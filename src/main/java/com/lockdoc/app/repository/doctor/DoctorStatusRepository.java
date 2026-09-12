package com.lockdoc.app.repository.doctor;

import com.lockdoc.app.entity.doctor.DoctorStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DoctorStatusRepository extends JpaRepository<DoctorStatus, Long> {

    /** Latest row for one (doctor, facility, date) triple — the "current status" (entity javadoc: append-only log). */
    Optional<DoctorStatus> findFirstByDoctorIdAndFacilityIdAndSessionDateOrderByCreatedDateDesc(
            Long doctorId, Long facilityId, LocalDate sessionDate);

    /** All of a facility's doctors' status rows for a date — latest-per-doctor resolution happens in the service. */
    List<DoctorStatus> findByFacilityIdAndSessionDateOrderByCreatedDateDesc(Long facilityId, LocalDate sessionDate);

    /** All of a doctor's status rows (across every facility) for a date — same latest-per-facility resolution in the service. */
    List<DoctorStatus> findByDoctorIdAndSessionDateOrderByCreatedDateDesc(Long doctorId, LocalDate sessionDate);

    /** Punctuality report (§17.7 #18) - every status change in range, oldest first, so the service can find each day's first "arrived" transition. */
    List<DoctorStatus> findByDoctorIdAndSessionDateBetweenOrderByCreatedDateAsc(Long doctorId, LocalDate from, LocalDate to);
}
