package com.lockdoc.app.dto.pharmacy;

import jakarta.validation.Valid;
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
public class StoreTransferRequest {

    @NotNull(message = "fromStoreId is required")
    private Long fromStoreId;

    @NotNull(message = "toStoreId is required")
    private Long toStoreId;

    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<StoreTransferItemRequest> items;
}
