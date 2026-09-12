package com.lockdoc.app.repository.op;

import com.lockdoc.app.entity.op.AppointmentWaitlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AppointmentWaitlistRepository extends JpaRepository<AppointmentWaitlist, Long> {

    List<AppointmentWaitlist> findByDoctorScheduleIdAndSessionDateAndPromotedAtIsNullOrderByJoinedAtAsc(
            Long doctorScheduleId, LocalDate sessionDate);

    Optional<AppointmentWaitlist> findByIdAndFacilityId(Long id, Long facilityId);
}
