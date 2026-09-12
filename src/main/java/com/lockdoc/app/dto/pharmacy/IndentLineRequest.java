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
public class IndentLineRequest {

    @NotNull(message = "medicineId is required")
    private Long medicineId;

    @NotNull(message = "requestedQty is required")
    @Positive(message = "requestedQty must be greater than zero")
    private Integer requestedQty;
}
