package com.lockdoc.app.dto.pharmacy;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Shared shape for both approve (approvedQty per line) and issue (issueQty per line). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IndentLineQtyRequest {

    @NotNull(message = "lineId is required")
    private Long lineId;

    @NotNull(message = "qty is required")
    @PositiveOrZero(message = "qty cannot be negative")
    private Integer qty;
}
