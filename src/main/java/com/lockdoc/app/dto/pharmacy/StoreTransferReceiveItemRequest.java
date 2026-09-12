package com.lockdoc.app.dto.pharmacy;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StoreTransferReceiveItemRequest {

    @NotNull(message = "lineId is required")
    private Long lineId;

    @NotNull(message = "receivedQty is required")
    @PositiveOrZero(message = "receivedQty cannot be negative")
    private Integer receivedQty;
}
