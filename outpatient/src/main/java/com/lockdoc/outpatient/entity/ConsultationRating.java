package com.lockdoc.outpatient.entity;

import com.lockdoc.common.entity.Doctor;
import com.lockdoc.common.entity.Facility;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Patient-satisfaction rating for a completed consultation (Master Spec
 * §17.7 #18's own gap - Doctor Reports had punctuality and volume but no
 * quality signal). Captured by Receptionist at the front desk once the OP
 * visit reaches COMPLETED - patients hand feedback to the desk on the way
 * out, there's no patient self-service portal in this build. One row per
 * {@link OpVisit}, edited in place rather than re-inserted (front desk
 * correcting a mis-tap, not a second review).
 */
@Entity
@Table(name = "consultation_ratings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "op_visit_id", nullable = false, unique = true)
    private OpVisit opVisit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id", nullable = false)
    private Facility facility;

    /** 1-5 stars. */
    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(name = "comment", length = 500)
    private String comment;

    @Column(name = "rated_by_user_id", nullable = false)
    private Long ratedByUserId;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedDate = LocalDateTime.now();
    }
}
