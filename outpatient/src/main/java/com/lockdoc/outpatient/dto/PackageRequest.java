package com.lockdoc.outpatient.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PackageRequest {

    @NotBlank(message = "name is required")
    private String name;

    private String code;

    @NotNull(message = "price is required")
    private BigDecimal price;

    @NotEmpty(message = "A package needs at least one included item")
    @Valid
    private List<PackageItemRequest> items;
}
