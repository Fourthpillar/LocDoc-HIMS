package com.lockdoc.outpatient.entity;

import com.lockdoc.common.entity.Facility;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Master Spec §6/§7.5 - append-only, multiple per bill. */
@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    public static final String PARTY_PATIENT = "PATIENT";
    public static final String PARTY_ORGANIZATION = "ORGANIZATION";
    public static final String PARTY_TPA = "TPA";

    public static final String TYPE_CASH = "CASH";
    public static final String TYPE_CARD_UPI = "CARD_UPI";
    public static final String TYPE_CHEQUE = "CHEQUE";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id", nullable = false)
    private Bill bill;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id", nullable = false)
    private Facility facility;

    @Column(name = "party_type", nullable = false, length = 20)
    @Builder.Default
    private String partyType = PARTY_PATIENT;

    @Column(name = "payment_type", nullable = false, length = 20)
    private String paymentType;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "paid_at", nullable = false)
    private LocalDateTime paidAt;

    @Column(name = "received_by_user_id", nullable = false)
    private Long receivedByUserId;

    @PrePersist
    protected void onCreate() {
        if (this.paidAt == null) {
            this.paidAt = LocalDateTime.now();
        }
    }
}
