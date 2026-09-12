package com.lockdoc.app.repository.doctor;

import com.lockdoc.app.entity.doctor.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    Optional<Prescription> findByOpVisitId(Long opVisitId);

    /** Medicines-prescribed report (§17.7 #18) - finalized prescriptions only, same rule as the consultation-count report. */
    @Query("SELECT p FROM Prescription p WHERE p.doctor.id = :doctorId AND p.isDraft = false "
            + "AND p.createdDate BETWEEN :from AND :to")
    List<Prescription> findCompletedInRange(@Param("doctorId") Long doctorId, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
