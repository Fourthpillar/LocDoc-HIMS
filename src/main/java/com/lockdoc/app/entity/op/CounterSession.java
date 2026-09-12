package com.lockdoc.app.entity.op;

import com.lockdoc.app.entity.Facility;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Counter session open/close & cash reconciliation (Master Spec §7.5/
 * §11.5, screens #12/#31) - shared shape for both OP and Pharmacy
 * counters, "identical behaviour... deliberately, since both share one
 * interaction model" - one entity, not two. {@link #systemTotal} is
 * computed at close time by summing this facility's CASH transactions
 * within [openedAt, closedAt] rather than accumulated via a live FK on
 * every Bill/SalesInvoice row - see CounterSessionService.
 */
@Entity
@Table(name = "counter_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CounterSession {

    public static final String TYPE_OP = "OP";
    public static final String TYPE_PHARMACY = "PHARMACY";

    public static final String STATUS_OPEN = "OPEN";
    public static final String STATUS_CLOSED = "CLOSED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id", nullable = false)
    private Facility facility;

    @Column(name = "counter_type", nullable = false, length = 10)
    private String counterType;

    @Column(name = "status", nullable = false, length = 10)
    @Builder.Default
    private String status = STATUS_OPEN;

    @Column(name = "opened_by_user_id", nullable = false)
    private Long openedByUserId;

    @Column(name = "opened_at", nullable = false)
    private LocalDateTime openedAt;

    @Column(name = "closed_by_user_id")
    private Long closedByUserId;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "declared_cash", precision = 10, scale = 2)
    private BigDecimal declaredCash;

    @Column(name = "system_total", precision = 10, scale = 2)
    private BigDecimal systemTotal;

    @Column(name = "variance", precision = 10, scale = 2)
    private BigDecimal variance;

    @PrePersist
    protected void onCreate() {
        if (this.openedAt == null) {
            this.openedAt = LocalDateTime.now();
        }
    }
}
