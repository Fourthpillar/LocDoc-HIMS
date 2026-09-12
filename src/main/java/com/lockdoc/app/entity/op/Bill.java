package com.lockdoc.app.entity.op;

import com.lockdoc.app.entity.Facility;
import com.lockdoc.app.entity.pharmacy.Patient;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Single source of truth for an OP billable event's money (Master Spec
 * §6/§7.5). {@code encounterType}+{@code encounterId} address the
 * billed-for record: PatientRegistration, OpVisit (consultation),
 * ProcedureBill (V39), or PackageSale (V41). Every financial table in
 * this system is append/reversal-only after posting (§6 invariant 2) - a
 * Payment adds a row and this bill's paid/due are recomputed, never
 * edited in place except for the running paid/due/status summary itself.
 */
@Entity
@Table(name = "bills")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bill {

    public static final String ENCOUNTER_REGISTRATION = "REGISTRATION";
    public static final String ENCOUNTER_CONSULTATION = "CONSULTATION";
    public static final String ENCOUNTER_PROCEDURE = "PROCEDURE";
    public static final String ENCOUNTER_PACKAGE = "PACKAGE";

    public static final String STATUS_UNPAID = "UNPAID";
    public static final String STATUS_PARTIALLY_PAID = "PARTIALLY_PAID";
    public static final String STATUS_PAID = "PAID";
    public static final String STATUS_CANCELLED = "CANCELLED";

    /** Nothing was ever paid against this bill, or it's already settled - the common case. */
    public static final String REFUND_NONE = "NONE";
    /** Cancelled with money still owed back to whoever paid it - surfaces on the Refunds screen until reception acts. */
    public static final String REFUND_PENDING = "PENDING";
    /** Reception has recorded the refund payment (a negative Payment row) against this bill. */
    public static final String REFUND_REFUNDED = "REFUNDED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id", nullable = false)
    private Facility facility;

    /** Null for an unregistered walk-in procedure bill (V44, §7.5) - every other encounter type still always sets this. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @Column(name = "bill_no", nullable = false, length = 30)
    private String billNo;

    @Column(name = "encounter_type", nullable = false, length = 20)
    private String encounterType;

    @Column(name = "encounter_id", nullable = false)
    private Long encounterId;

    @Column(name = "gross", nullable = false, precision = 10, scale = 2)
    private BigDecimal gross;

    @Column(name = "discount", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(name = "tax", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal tax = BigDecimal.ZERO;

    @Column(name = "net", nullable = false, precision = 10, scale = 2)
    private BigDecimal net;

    @Column(name = "paid", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal paid = BigDecimal.ZERO;

    @Column(name = "due", nullable = false, precision = 10, scale = 2)
    private BigDecimal due;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = STATUS_UNPAID;

    @Column(name = "refund_status", nullable = false, length = 20)
    @Builder.Default
    private String refundStatus = REFUND_NONE;

    @Column(name = "created_by_user_id", nullable = false)
    private Long createdByUserId;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
    }
}
