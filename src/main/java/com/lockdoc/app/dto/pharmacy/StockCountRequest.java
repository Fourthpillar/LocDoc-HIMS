package com.lockdoc.app.dto.pharmacy;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/** Raises a count for every currently-in-stock batch at the store, snapshotting system_qty (§11.4). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockCountRequest {

    @NotNull(message = "storeId is required")
    private Long storeId;

    @NotNull(message = "countDate is required")
    private LocalDate countDate;
}
