package com.lockdoc.outpatient.entity;

import com.lockdoc.common.entity.Facility;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * One row per facility, self-service (Master Spec §6/§7.2) - Hospital/
 * Clinic Admin sets and edits this from their own login, same
 * self-service pattern as ApprovalPolicy.
 */
@Entity
@Table(name = "registration_fee_configs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationFeeConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id", nullable = false, unique = true)
    private Facility facility;

    @Column(name = "first_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal firstFee;

    @Column(name = "re_registration_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal reRegistrationFee;

    /** Null means a registration taken here never expires — the facility charges once, for life (§7.2). */
    @Column(name = "validity_days")
    private Integer validityDays;

    @Column(name = "updated_date", nullable = false)
    private LocalDateTime updatedDate;

    @PrePersist
    @PreUpdate
    protected void touch() {
        this.updatedDate = LocalDateTime.now();
    }
}
