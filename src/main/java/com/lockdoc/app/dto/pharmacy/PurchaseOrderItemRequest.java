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
public class PurchaseOrderItemRequest {

    @NotNull(message = "Medicine is required")
    private Long medicineId;

    @NotNull(message = "Ordered quantity is required")
    @Positive(message = "Ordered quantity must be positive")
    private Integer orderedQty;

    @NotNull(message = "Rate is required")
    @Positive(message = "Rate must be positive")
    private BigDecimal rate;

    @PositiveOrZero(message = "Tax percent must not be negative")
    private BigDecimal taxPercent;
}
