package com.lockdoc.outpatient.repository;

import com.lockdoc.outpatient.entity.Appointment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    Optional<Appointment> findByIdAndFacilityId(Long id, Long facilityId);

    List<Appointment> findByFacilityIdAndAppointmentTsBetweenOrderByAppointmentTsAsc(Long facilityId, LocalDateTime from, LocalDateTime to);

    List<Appointment> findByPatientIdOrderByAppointmentTsDesc(Long patientId);

    /** Ending a doctor-facility mapping must not silently orphan future bookings (Master Spec §8.1) - see DoctorFacilityMappingService.end(). */
    boolean existsByDoctorIdAndFacilityIdAndAppointmentTsAfterAndStatusIn(
            Long doctorId, Long facilityId, LocalDateTime after, List<String> statuses);

    /** Which live appointments a proposed schedule exception would affect (Availability Planner, §8.2) - see DoctorAvailabilityService. */
    List<Appointment> findByDoctorIdAndFacilityIdAndAppointmentTsBetweenAndStatusIn(
            Long doctorId, Long facilityId, LocalDateTime from, LocalDateTime to, List<String> statuses);

    /** Live bookings on one recurring session from a point in time onward - what ending that session would affect (Availability Planner, §8.2). */
    List<Appointment> findByDoctorScheduleIdAndAppointmentTsGreaterThanEqualAndStatusInOrderByAppointmentTsAsc(
            Long doctorScheduleId, LocalDateTime from, List<String> statuses);

    /** Read-only booked count per occurrence for the planner grid - deliberately not the PESSIMISTIC_WRITE lock findLiveForUpdate() takes for an actual booking. */
    long countByDoctorScheduleIdAndAppointmentTsBetweenAndStatusIn(
            Long doctorScheduleId, LocalDateTime from, LocalDateTime to, List<String> statuses);

    /**
     * Counts live (not cancelled/no-show/rescheduled-away) bookings for a
     * schedule+date window - the capacity+overbook check (§6 invariant 5).
     * PESSIMISTIC_WRITE locks the matching rows so two concurrent bookings
     * for the last slot can't both read the same count and both succeed.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Appointment a WHERE a.doctorSchedule.id = :scheduleId "
            + "AND a.appointmentTs BETWEEN :from AND :to "
            + "AND a.status IN ('BOOKED', 'ARRIVED', 'IN_CONSULTATION', 'COMPLETED')")
    List<Appointment> findLiveForUpdate(@Param("scheduleId") Long scheduleId,
                                         @Param("from") LocalDateTime from,
                                         @Param("to") LocalDateTime to);
}
