package com.lockdoc.app.entity.op;

import com.lockdoc.app.entity.Facility;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Self-service (Master Spec §6/§7.5/§15.1) - Hospital/Clinic Admin sets
 * and edits these from their own login. Only {@link #TYPE_DISCOUNT} is
 * actually read by BillingService in this pass - see V23's migration
 * comment for why cancellation doesn't use a threshold here.
 */
@Entity
@Table(name = "approval_policies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalPolicy {

    public static final String TYPE_DISCOUNT = "DISCOUNT";
    public static final String TYPE_REFUND = "REFUND";
    public static final String TYPE_CANCELLATION = "CANCELLATION";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id", nullable = false)
    private Facility facility;

    @Column(name = "approval_type", nullable = false, length = 30)
    private String approvalType;

    @Column(name = "threshold_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal thresholdValue;

    @Column(name = "updated_date", nullable = false)
    private LocalDateTime updatedDate;

    @PrePersist
    @PreUpdate
    protected void touch() {
        this.updatedDate = LocalDateTime.now();
    }
}
