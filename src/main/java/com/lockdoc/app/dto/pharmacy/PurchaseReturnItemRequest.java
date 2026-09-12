package com.lockdoc.app.dto.pharmacy;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseReturnItemRequest {

    @NotNull(message = "purchaseItemId is required")
    private Long purchaseItemId;

    @NotNull(message = "returnedQty is required")
    @Positive(message = "returnedQty must be greater than zero")
    private Integer returnedQty;
}
