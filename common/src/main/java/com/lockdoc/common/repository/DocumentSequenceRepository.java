package com.lockdoc.common.repository;

import com.lockdoc.common.entity.DocumentSequence;
import com.lockdoc.common.entity.DocumentSequenceId;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DocumentSequenceRepository extends JpaRepository<DocumentSequence, DocumentSequenceId> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ds FROM DocumentSequence ds WHERE ds.facilityId = :facilityId AND ds.docType = :docType AND ds.year = :year")
    Optional<DocumentSequence> findForUpdate(@Param("facilityId") Long facilityId, @Param("docType") String docType, @Param("year") Integer year);
}
