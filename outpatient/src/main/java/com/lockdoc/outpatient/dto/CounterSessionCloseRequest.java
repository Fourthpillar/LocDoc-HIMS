package com.lockdoc.outpatient.dto;

import jakarta.validation.constraints.NotNull;
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
public class CounterSessionCloseRequest {

    @NotNull(message = "declaredCash is required")
    @PositiveOrZero(message = "declaredCash cannot be negative")
    private BigDecimal declaredCash;
}
