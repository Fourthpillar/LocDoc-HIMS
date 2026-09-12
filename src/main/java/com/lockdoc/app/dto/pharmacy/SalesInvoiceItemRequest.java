package com.lockdoc.app.dto.pharmacy;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SalesInvoiceItemRequest {

    @NotNull(message = "Medicine is required")
    private Long medicineId;

    @NotNull(message = "Medicine batch is required")
    private Long medicineBatchId;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Integer qty;

    @NotNull(message = "Rate is required")
    @Positive(message = "Rate must be positive")
    private BigDecimal rate;

    @PositiveOrZero(message = "Tax percent must not be negative")
    private BigDecimal taxPercent;

    @PositiveOrZero(message = "Discount amount must not be negative")
    private BigDecimal discountAmount;

    /** Soft reference to the PrescriptionLine this line fulfils (Master Spec §11.6), when dispensing against one. */
    private Long prescriptionLineId;

    /**
     * A dispensing user's reason for proceeding past a soft-stop warning
     * (allergy/high-alert/duplicate-therapy, §11.5) already surfaced by a
     * prior SafetyCheckException on this same line - present only on the
     * resubmitted, confirmed request.
     */
    private String overrideReason;

    /** Mandatory when the medicine is a scheduled drug (Medicine.requiresPrescriberCapture(), §11.5) - validated in SalesService. */
    private String prescriberName;

    private String prescriberRegistrationNumber;
}
