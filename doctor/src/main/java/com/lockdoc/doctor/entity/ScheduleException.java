package com.lockdoc.doctor.entity;

import com.lockdoc.common.entity.Doctor;
import com.lockdoc.common.entity.Facility;
import com.lockdoc.outpatient.entity.DoctorSchedule;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * A doctor blocking part or all of their own recurring schedule on one
 * date (Master Spec §8.2/§6, screen #14 Availability Planner) - leave,
 * a single session cancellation, a holiday, or a drag-initiated move to a
 * new time on that date. {@link #doctorSchedule} null means every session
 * that doctor has at {@link #facility} on {@link #exceptionDate} is
 * blocked; set means just that one session (a {@link #TYPE_SESSION_MOVE}
 * always has it set - moving the whole day isn't a supported gesture).
 */
@Entity
@Table(name = "schedule_exceptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleException {

    public static final String TYPE_LEAVE = "LEAVE";
    public static final String TYPE_SESSION_CANCEL = "SESSION_CANCEL";
    public static final String TYPE_HOLIDAY = "HOLIDAY";
    public static final String TYPE_SESSION_MOVE = "SESSION_MOVE";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id", nullable = false)
    private Facility facility;

    /** Null = blocks every session this doctor has at this facility on exceptionDate (whole-day leave/holiday). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_schedule_id")
    private DoctorSchedule doctorSchedule;

    @Column(name = "exception_date", nullable = false)
    private LocalDate exceptionDate;

    @Column(name = "exception_type", nullable = false, length = 20)
    private String exceptionType;

    @Column(name = "reason", length = 300)
    private String reason;

    /** Set only for {@link #TYPE_SESSION_MOVE} - the proposed new time for this one occurrence. */
    @Column(name = "new_start_time")
    private LocalTime newStartTime;

    @Column(name = "new_end_time")
    private LocalTime newEndTime;

    @Column(name = "created_by_user_id", nullable = false)
    private Long createdByUserId;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
    }
}
