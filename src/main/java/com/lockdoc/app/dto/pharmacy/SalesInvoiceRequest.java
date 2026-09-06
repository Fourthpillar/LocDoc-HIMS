package com.lockdoc.app.dto.pharmacy;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
public class SalesInvoiceRequest {

    private Long patientId;

    private String walkInCustomerName;

    private String walkInCustomerPhone;

    @NotNull(message = "Sale date is required")
    private LocalDate saleDate;

    @NotBlank(message = "Payment mode is required")
    private String paymentMode;

    @PositiveOrZero(message = "Amount paid must not be negative")
    private BigDecimal amountPaid;

    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<SalesInvoiceItemRequest> items;
}
