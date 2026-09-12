package com.lockdoc.app.dto.op;

import jakarta.validation.constraints.NotBlank;
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
public class ApprovalPolicyRequest {

    @NotBlank(message = "approvalType is required")
    private String approvalType;

    @NotNull(message = "thresholdValue is required")
    @PositiveOrZero(message = "thresholdValue cannot be negative")
    private BigDecimal thresholdValue;
}
