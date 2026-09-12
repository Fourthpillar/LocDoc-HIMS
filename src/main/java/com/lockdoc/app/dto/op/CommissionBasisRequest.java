package com.lockdoc.app.dto.op;

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
public class CommissionBasisRequest {

    @NotBlank(message = "partyType is required")
    private String partyType;

    @NotBlank(message = "partyName is required")
    private String partyName;

    @NotBlank(message = "basis is required")
    private String basis;

    @NotNull(message = "value is required")
    @Positive(message = "value must be greater than zero")
    private BigDecimal value;

    private String appliesTo;
}
