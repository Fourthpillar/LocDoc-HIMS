package com.lockdoc.pharmacy.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseRequest {

    private Long purchaseOrderId;

    @NotNull(message = "Supplier is required")
    private Long supplierId;

    @NotNull(message = "Purchase date is required")
    private LocalDate purchaseDate;

    private String supplierInvoiceNumber;

    private LocalDate supplierInvoiceDate;

    private String remarks;

    @PositiveOrZero(message = "Amount paid must not be negative")
    private BigDecimal amountPaid;

    private LocalDate dueDate;

    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<PurchaseItemRequest> items;
}
