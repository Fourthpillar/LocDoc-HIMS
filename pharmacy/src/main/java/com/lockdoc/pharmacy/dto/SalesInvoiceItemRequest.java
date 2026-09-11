package com.lockdoc.pharmacy.dto;

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
}
