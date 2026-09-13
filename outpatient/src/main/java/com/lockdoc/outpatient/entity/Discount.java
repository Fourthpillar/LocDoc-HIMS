package com.lockdoc.outpatient.entity;

import com.lockdoc.common.entity.Facility;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Bill-level discount with reason (Master Spec §6/§7.5). Below the
 * facility's configured {@link ApprovalPolicy} threshold it posts
 * straight to {@link #STATUS_APPROVED} and reduces the bill immediately;
 * above it, it starts {@link #STATUS_PENDING_APPROVAL} and does not
 * reduce the payable amount until a Hospital/Clinic Admin approves it.
 * Line-level discounts are deferred alongside BillLine/procedure billing.
 */
@Entity
@Table(name = "discounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Discount {

    public static final String KIND_PERCENT = "PERCENT";
    public static final String KIND_FIXED = "FIXED";

    public static final String STATUS_PENDING_APPROVAL = "PENDING_APPROVAL";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id", nullable = false)
    private Bill bill;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id", nullable = false)
    private Facility facility;

    @Column(name = "discount_kind", nullable = false, length = 10)
    private String discountKind;

    /** The raw input - a percent (0-100) or a fixed rupee value, per discountKind. */
    @Column(name = "discount_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal value;

    /** The resolved rupee amount actually applied to the bill. */
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "reason", nullable = false, length = 300)
    private String reason;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = STATUS_APPROVED;

    @Column(name = "requested_by_user_id", nullable = false)
    private Long requestedByUserId;

    @Column(name = "approved_by_user_id")
    private Long approvedByUserId;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
    }
}
