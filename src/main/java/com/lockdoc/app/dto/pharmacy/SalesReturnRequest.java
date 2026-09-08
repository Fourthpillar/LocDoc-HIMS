package com.lockdoc.app.dto.pharmacy;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SalesReturnRequest {

    @NotNull(message = "Sales invoice is required")
    private Long salesInvoiceId;

    @NotNull(message = "Return date is required")
    private LocalDate returnDate;

    private String reason;

    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<SalesReturnItemRequest> items;
}
