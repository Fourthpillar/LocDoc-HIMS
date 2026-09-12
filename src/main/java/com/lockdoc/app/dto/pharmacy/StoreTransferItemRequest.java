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
public class StoreTransferItemRequest {

    @NotNull(message = "medicineId is required")
    private Long medicineId;

    @NotNull(message = "batchNo is required")
    private String batchNo;

    @NotNull(message = "issuedQty is required")
    @Positive(message = "issuedQty must be greater than zero")
    private Integer issuedQty;
}
