package com.lockdoc.app.repository.dataprotection;

import com.lockdoc.app.entity.dataprotection.Consent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConsentRepository extends JpaRepository<Consent, Long> {
    List<Consent> findByFacilityIdAndPatientIdOrderByCapturedAtDesc(Long facilityId, Long patientId);
}
