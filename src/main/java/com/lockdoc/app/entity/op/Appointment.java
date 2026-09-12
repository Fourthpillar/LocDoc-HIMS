package com.lockdoc.app.entity.op;

import com.lockdoc.app.entity.Doctor;
import com.lockdoc.app.entity.Facility;
import com.lockdoc.app.entity.pharmacy.Patient;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** Master Spec §6/§7.3. */
@Entity
@Table(name = "appointments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment {

    public static final String STATUS_BOOKED = "BOOKED";
    public static final String STATUS_ARRIVED = "ARRIVED";
    public static final String STATUS_IN_CONSULTATION = "IN_CONSULTATION";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_CANCELLED = "CANCELLED";
    public static final String STATUS_NO_SHOW = "NO_SHOW";
    public static final String STATUS_RESCHEDULED = "RESCHEDULED";

    /** What the slot is for — a consultation bills the doctor's fee on arrival, a procedure does not (§7.5). */
    public static final String PURPOSE_CONSULTATION = "CONSULTATION";
    public static final String PURPOSE_PROCEDURE = "PROCEDURE";

    public static final String CHANNEL_FRONT_DESK = "FRONT_DESK";
    public static final String CHANNEL_PHONE = "PHONE";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id", nullable = false)
    private Facility facility;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_schedule_id", nullable = false)
    private DoctorSchedule doctorSchedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "appointment_ts", nullable = false)
    private LocalDateTime appointmentTs;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = STATUS_BOOKED;

    @Column(name = "purpose", nullable = false, length = 20)
    @Builder.Default
    private String purpose = PURPOSE_CONSULTATION;

    @Column(name = "channel", nullable = false, length = 20)
    @Builder.Default
    private String channel = CHANNEL_FRONT_DESK;

    @Column(name = "cancel_reason", length = 300)
    private String cancelReason;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
    }
}
