package com.lockdoc.app.dto.op;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PackageItemRequest {

    @NotNull(message = "billableItemId is required")
    private Long billableItemId;

    @NotNull(message = "includedQty is required")
    @Min(value = 1, message = "includedQty must be at least 1")
    private Integer includedQty;
}
