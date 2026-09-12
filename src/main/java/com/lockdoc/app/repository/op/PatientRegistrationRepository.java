package com.lockdoc.app.repository.op;

import com.lockdoc.app.entity.op.PatientRegistration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PatientRegistrationRepository extends JpaRepository<PatientRegistration, Long> {

    /** Most recent registration for a patient at a facility - decides first-vs-re-registration and expiry. */
    List<PatientRegistration> findByPatientIdAndFacilityIdOrderByRegisteredAtDesc(Long patientId, Long facilityId);

    boolean existsByFacilityIdAndRegistrationNo(Long facilityId, String registrationNo);

    /** Registrations report (§17.7 #12a). */
    List<PatientRegistration> findByFacilityIdAndRegisteredAtBetweenOrderByRegisteredAtAsc(Long facilityId, LocalDateTime from, LocalDateTime to);
}
