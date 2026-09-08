package com.lockdoc.app.dto.pharmacy;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseItemRequest {

    @NotNull(message = "Medicine is required")
    private Long medicineId;

    private Long purchaseOrderItemId;

    @NotBlank(message = "Batch number is required")
    private String batchNo;

    @NotNull(message = "Expiry date is required")
    private LocalDate expiryDate;

    @NotNull(message = "Received quantity is required")
    @Positive(message = "Received quantity must be positive")
    private Integer receivedQty;

    @PositiveOrZero(message = "Free quantity must not be negative")
    private Integer freeQty;

    @NotNull(message = "Rate is required")
    @Positive(message = "Rate must be positive")
    private BigDecimal rate;

    @PositiveOrZero(message = "Tax percent must not be negative")
    private BigDecimal taxPercent;

    @NotNull(message = "MRP is required")
    @Positive(message = "MRP must be positive")
    private BigDecimal mrp;

    @NotNull(message = "Sale rate is required")
    @Positive(message = "Sale rate must be positive")
    private BigDecimal saleRate;
}
