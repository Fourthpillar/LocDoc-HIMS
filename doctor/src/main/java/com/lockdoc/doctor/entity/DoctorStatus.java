package com.lockdoc.doctor.entity;

import com.lockdoc.common.entity.Facility;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Live status event log (Master Spec §8.3/§6) — append-only, like
 * {@code AuditLog}: every status change is a new row, never an update, so
 * the full "DoctorStatus changes" history §17.2 requires stays intact.
 * The "current" status for a (doctor, facility, sessionDate) is simply
 * the most recently created row for that triple.
 *
 * Status phase only, as scoped to build order step 4: {@code source} is
 * always {@link #SOURCE_SELF} here — {@link #SOURCE_RECEPTION} (the
 * override control) ships attached to step 5's front-desk day list, and
 * {@link #SOURCE_GEOFENCE} is deferred with the rest of §8.4's
 * GPS/Mapbox work to the mobile-app phase (build order step 10). Both
 * constants are declared now so the enum is complete and step 5 doesn't
 * have to revisit this entity to add a value.
 */
@Entity
@Table(name = "doctor_statuses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorStatus {

    public static final String UNAVAILABLE = "UNAVAILABLE";
    public static final String AVAILABLE = "AVAILABLE";
    public static final String IN_TRANSIT = "IN_TRANSIT";
    public static final String AT_FACILITY = "AT_FACILITY";
    public static final String IN_CONSULTATION = "IN_CONSULTATION";
    public static final String IN_ROUNDS = "IN_ROUNDS";
    public static final String DAY_COMPLETE = "DAY_COMPLETE";

    public static final String SOURCE_SELF = "SELF";
    public static final String SOURCE_RECEPTION = "RECEPTION";
    public static final String SOURCE_GEOFENCE = "GEOFENCE";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id", nullable = false)
    private Facility facility;

    @Column(name = "session_date", nullable = false)
    private LocalDate sessionDate;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "source", nullable = false, length = 20)
    @Builder.Default
    private String source = SOURCE_SELF;

    @Column(name = "set_by_user_id", nullable = false)
    private Long setByUserId;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
    }
}
