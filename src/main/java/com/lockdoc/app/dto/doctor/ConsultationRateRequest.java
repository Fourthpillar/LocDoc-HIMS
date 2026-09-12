package com.lockdoc.app.dto.doctor;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/** Doctor proposing (or revising) their own consultation fee at a facility (Master Spec §8.6). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationRateRequest {

    @NotNull(message = "facilityId is required")
    private Long facilityId;

    /** Defaults to DIRECT server-side when omitted — Organization/TPA masters don't exist yet (§7.2 is a later step). */
    private String orgType;

    /** Defaults to DAY server-side when omitted. */
    private String dayNightIndicator;

    @NotNull(message = "totalAmount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "totalAmount must be greater than zero")
    private BigDecimal totalAmount;

    @DecimalMin(value = "0.0", message = "hospitalPercent cannot be negative")
    private BigDecimal hospitalPercent;
}
