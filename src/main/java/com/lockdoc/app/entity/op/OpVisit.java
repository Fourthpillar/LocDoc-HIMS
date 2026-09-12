package com.lockdoc.app.entity.op;

import com.lockdoc.app.entity.Doctor;
import com.lockdoc.app.entity.Facility;
import com.lockdoc.app.entity.pharmacy.Patient;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * A patient's visit to a doctor at a facility (Master Spec §6/§7.4).
 * Deliberately non-clinical here (§7.1) - the clinical narrative
 * (ConsultationNote/Prescription) is the Doctor Module's own screen,
 * build order step 6.
 */
@Entity
@Table(name = "op_visits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpVisit {

    public static final String STATUS_ARRIVED = "ARRIVED";
    public static final String STATUS_IN_CONSULTATION = "IN_CONSULTATION";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    public static final String VISIT_NEW = "NEW";
    public static final String VISIT_FOLLOW_UP = "FOLLOW_UP";
    public static final String VISIT_SECOND_OPINION = "SECOND_OPINION";
    public static final String VISIT_CAMP = "CAMP";
    public static final String VISIT_CASUALTY_OP = "CASUALTY_OP";
    public static final String VISIT_VACCINATION = "VACCINATION";
    public static final String VISIT_PROCEDURE_ONLY = "PROCEDURE_ONLY";
    public static final String VISIT_CORPORATE_HEALTH_CHECK = "CORPORATE_HEALTH_CHECK";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "op_no", nullable = false, length = 30)
    private String opNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id", nullable = false)
    private Facility facility;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    // Nullable - a walk-in visit with no prior appointment (§7.3: "a pure
    // walk-in day must work perfectly with appointments off").
    @Column(name = "appointment_id")
    private Long appointmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(name = "org_type", nullable = false, length = 20)
    @Builder.Default
    private String orgType = "DIRECT";

    @Column(name = "visit_type", nullable = false, length = 30)
    @Builder.Default
    private String visitType = VISIT_NEW;

    @Column(name = "weight_kg", precision = 6, scale = 2)
    private BigDecimal weightKg;

    @Column(name = "height_cm", precision = 6, scale = 2)
    private BigDecimal heightCm;

    @Column(name = "temperature_f", precision = 5, scale = 2)
    private BigDecimal temperatureF;

    @Column(name = "bp", length = 20)
    private String bp;

    @Column(name = "attendant_name", length = 150)
    private String attendantName;

    @Column(name = "attendant_mobile", length = 20)
    private String attendantMobile;

    @Column(name = "attendant_relation", length = 50)
    private String attendantRelation;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = STATUS_ARRIVED;

    /** Set only when status is CANCELLED (§5 principle 4 - a cancellation is never unexplained). */
    @Column(name = "cancel_reason", length = 300)
    private String cancelReason;

    @Column(name = "mlc_flag", nullable = false)
    @Builder.Default
    private Boolean mlcFlag = false;

    @Column(name = "mlc_police_station", length = 150)
    private String mlcPoliceStation;

    @Column(name = "mlc_number", length = 50)
    private String mlcNumber;

    @Column(name = "arrived_ts", nullable = false)
    private LocalDateTime arrivedTs;

    @Column(name = "consult_start_ts")
    private LocalDateTime consultStartTs;

    @Column(name = "consult_end_ts")
    private LocalDateTime consultEndTs;

    /** Referral attribution (§7.4/§8.1, V46) - optional, for the Commission report; LocDoc-HIMS reports the computed share, never transfers money. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referral_doctor_id")
    private ReferralDoctor referralDoctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pro_id")
    private Pro pro;

    @PrePersist
    protected void onCreate() {
        if (this.arrivedTs == null) {
            this.arrivedTs = LocalDateTime.now();
        }
    }
}
