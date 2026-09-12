package com.lockdoc.app.dto.pharmacy;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseReturnRequest {

    @NotNull(message = "purchaseId is required")
    private Long purchaseId;

    @NotBlank(message = "reason is required")
    private String reason;

    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<PurchaseReturnItemRequest> items;
}
