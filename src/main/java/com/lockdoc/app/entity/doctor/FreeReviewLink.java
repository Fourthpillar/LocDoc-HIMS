package com.lockdoc.app.entity.doctor;

import com.lockdoc.app.entity.Doctor;
import com.lockdoc.app.entity.Facility;
import com.lockdoc.app.entity.op.OpVisit;
import com.lockdoc.app.entity.pharmacy.Patient;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * One free-review visit, anchored back to the paid consultation it's
 * "against" (Master Spec §7.4 - "Free review — 2nd of 3, against
 * consultation OP/2026/01432"; §6's own entity list names this
 * FreeReviewLink, "missing from every earlier revision"). Created at
 * billing time in OpVisitService when a visit qualifies under the
 * doctor's {@link FreeReviewPolicy}; never created retroactively.
 */
@Entity
@Table(name = "free_review_links")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FreeReviewLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id", nullable = false)
    private Facility facility;

    /** The free visit itself. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "op_visit_id", nullable = false)
    private OpVisit opVisit;

    /** The paid consultation this free review is counted against. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_op_visit_id", nullable = false)
    private OpVisit originalOpVisit;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
    }
}
