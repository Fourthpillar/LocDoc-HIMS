package com.lockdoc.app.repository.doctor;

import com.lockdoc.app.entity.doctor.FreeReviewLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface FreeReviewLinkRepository extends JpaRepository<FreeReviewLink, Long> {

    long countByOriginalOpVisitId(Long originalOpVisitId);

    /** Free-reviews report (§17.7 #12a) - "Nth of M against OP/...", computed from these rows' ordering. */
    List<FreeReviewLink> findByFacilityIdAndCreatedDateBetweenOrderByCreatedDateAsc(Long facilityId, LocalDateTime from, LocalDateTime to);

    List<FreeReviewLink> findByOriginalOpVisitIdOrderByCreatedDateAsc(Long originalOpVisitId);
}
