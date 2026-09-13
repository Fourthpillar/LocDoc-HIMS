package com.lockdoc.outpatient.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DiscountRequest {

    /** PERCENT or FIXED. */
    @NotBlank(message = "discountKind is required")
    private String discountKind;

    @NotNull(message = "value is required")
    @Positive(message = "value must be greater than zero")
    private BigDecimal value;

    @NotBlank(message = "reason is required")
    private String reason;
}
