package com.lockdoc.doctor.repository;

import com.lockdoc.doctor.entity.ScheduleException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ScheduleExceptionRepository extends JpaRepository<ScheduleException, Long> {

    List<ScheduleException> findByDoctorIdAndExceptionDateBetweenOrderByExceptionDateAsc(Long doctorId, LocalDate from, LocalDate to);

    List<ScheduleException> findByFacilityIdAndExceptionDateBetweenOrderByExceptionDateAsc(Long facilityId, LocalDate from, LocalDate to);

    Optional<ScheduleException> findByIdAndDoctorId(Long id, Long doctorId);

    /** Whole-day block already exists for this doctor+facility+date (doctorScheduleId IS NULL). */
    boolean existsByDoctorIdAndFacilityIdAndExceptionDateAndDoctorScheduleIdIsNull(Long doctorId, Long facilityId, LocalDate exceptionDate);

    /** A specific session is already individually blocked on this date. */
    boolean existsByDoctorScheduleIdAndExceptionDate(Long doctorScheduleId, LocalDate exceptionDate);

    /** One session's blocks/moves after a date - dropped when the session is ended, since those dates no longer exist. */
    List<ScheduleException> findByDoctorScheduleIdAndExceptionDateAfter(Long doctorScheduleId, LocalDate date);
}
