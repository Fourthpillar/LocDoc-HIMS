package com.lockdoc.outpatient.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BillableItemRequest {

    @NotBlank(message = "itemType is required")
    private String itemType;

    @NotBlank(message = "name is required")
    private String name;

    private String code;

    @NotNull(message = "rateDirect is required")
    private BigDecimal rateDirect;

    private BigDecimal rateOrganization;
    private BigDecimal rateTpa;
}
