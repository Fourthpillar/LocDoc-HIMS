package com.lockdoc.outpatient.dto;

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
public class RegistrationFeeConfigRequest {

    @NotNull(message = "firstFee is required")
    @Positive(message = "firstFee must be greater than zero")
    private BigDecimal firstFee;

    @NotNull(message = "reRegistrationFee is required")
    @Positive(message = "reRegistrationFee must be greater than zero")
    private BigDecimal reRegistrationFee;

    /** Omit (null) for registrations that never expire; otherwise the number of days one stays valid. */
    @Positive(message = "validityDays must be greater than zero")
    private Integer validityDays;
}
