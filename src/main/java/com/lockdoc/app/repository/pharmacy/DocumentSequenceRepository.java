package com.lockdoc.app.repository.pharmacy;

import com.lockdoc.app.entity.pharmacy.DocumentSequence;
import com.lockdoc.app.entity.pharmacy.DocumentSequenceId;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DocumentSequenceRepository extends JpaRepository<DocumentSequence, DocumentSequenceId> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ds FROM DocumentSequence ds WHERE ds.docType = :docType AND ds.year = :year")
    Optional<DocumentSequence> findForUpdate(@Param("docType") String docType, @Param("year") Integer year);
}
