package com.lockdoc.doctor.repository;

import com.lockdoc.doctor.entity.ConsultationNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ConsultationNoteRepository extends JpaRepository<ConsultationNote, Long> {

    Optional<ConsultationNote> findByOpVisitId(Long opVisitId);

    /** Prior visit history with this doctor (§8.7) - completed notes only, newest first. */
    @Query("SELECT cn FROM ConsultationNote cn WHERE cn.doctor.id = :doctorId AND cn.opVisit.patient.id = :patientId "
            + "AND cn.isDraft = false ORDER BY cn.createdDate DESC")
    List<ConsultationNote> findCompletedHistory(@Param("doctorId") Long doctorId, @Param("patientId") Long patientId);

    /** Consultation count report (§17.7 #18) - completed notes only, a draft was never the visit's final record (§8.7). */
    @Query("SELECT cn FROM ConsultationNote cn WHERE cn.doctor.id = :doctorId AND cn.isDraft = false "
            + "AND cn.createdDate BETWEEN :from AND :to ORDER BY cn.createdDate ASC")
    List<ConsultationNote> findCompletedInRange(@Param("doctorId") Long doctorId, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
