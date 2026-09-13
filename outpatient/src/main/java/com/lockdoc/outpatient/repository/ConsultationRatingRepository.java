package com.lockdoc.outpatient.repository;

import com.lockdoc.outpatient.entity.ConsultationRating;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConsultationRatingRepository extends JpaRepository<ConsultationRating, Long> {

    Optional<ConsultationRating> findByOpVisitId(Long opVisitId);

    /** Doctor Reports (§17.7 #18) - batched lookup for a whole date range's worth of completed visits. */
    List<ConsultationRating> findByOpVisitIdIn(List<Long> opVisitIds);
}
