package com.lockdoc.app.repository.op;

import com.lockdoc.app.entity.op.Discount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DiscountRepository extends JpaRepository<Discount, Long> {
    List<Discount> findByBillIdOrderByCreatedDateDesc(Long billId);
    List<Discount> findByFacilityIdAndStatusOrderByCreatedDateAsc(Long facilityId, String status);
    Optional<Discount> findByIdAndFacilityId(Long id, Long facilityId);

    /** Discounts-given report (§17.7 #12a). */
    List<Discount> findByFacilityIdAndCreatedDateBetweenOrderByCreatedDateAsc(Long facilityId, LocalDateTime from, LocalDateTime to);
}
