package com.lockdoc.app.entity.pharmacy;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sales_invoice_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesInvoiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_invoice_id", nullable = false)
    private SalesInvoice salesInvoice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_batch_id", nullable = false)
    private MedicineBatch medicineBatch;

    @Column(name = "qty", nullable = false)
    private Integer qty;

    @Column(name = "rate", nullable = false, precision = 10, scale = 2)
    private BigDecimal rate;

    @Column(name = "tax_percent", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal taxPercent = BigDecimal.ZERO;

    @Column(name = "discount_amount", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    /**
     * Soft reference to the doctor-authored line this sale fulfils
     * (Master Spec §11.6/§8.7) - not a JPA relation, Prescription lives
     * in the Doctor Module's own bounded context (same reasoning as
     * MedicineBatch.sourcePurchaseItemId).
     */
    @Column(name = "prescription_line_id")
    private Long prescriptionLineId;

    /** Records a soft-stop override (allergy/high-alert/duplicate-therapy) - never silent, per §11.5. */
    @Column(name = "override_reason", length = 300)
    private String overrideReason;

    /** Mandatory (enforced in SalesService, not the schema) only when the medicine is a scheduled drug (§11.5). */
    @Column(name = "prescriber_name", length = 150)
    private String prescriberName;

    @Column(name = "prescriber_registration_number", length = 100)
    private String prescriberRegistrationNumber;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdDate = now;
        this.updatedDate = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedDate = LocalDateTime.now();
    }
}
