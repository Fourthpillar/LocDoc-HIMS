package com.lockdoc.common.repository;

import com.lockdoc.common.entity.ConsultationRate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConsultationRateRepository extends JpaRepository<ConsultationRate, Long> {

    List<ConsultationRate> findByDoctorIdOrderByCreatedDateDesc(Long doctorId);

    List<ConsultationRate> findByDoctorIdAndFacilityIdOrderByCreatedDateDesc(Long doctorId, Long facilityId);

    /** Feeds build order step 5's approval queue and OP billing rate lookup — not called by anything in step 4 itself. */
    List<ConsultationRate> findByFacilityIdAndStatusOrderByCreatedDateDesc(Long facilityId, String status);
}
