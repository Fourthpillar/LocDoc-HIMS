package com.lockdoc.app.dto.pharmacy;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MedicineRequest {

    @NotBlank(message = "Code is required")
    private String code;

    @NotBlank(message = "Name is required")
    private String name;

    private String genericName;

    private String manufacturer;

    private String category;

    @NotBlank(message = "UOM is required")
    private String uom;

    private String hsnCode;

    @NotNull(message = "Tax percent is required")
    @PositiveOrZero(message = "Tax percent must not be negative")
    private BigDecimal taxPercent;

    @NotNull(message = "Reorder level is required")
    @PositiveOrZero(message = "Reorder level must not be negative")
    private Integer reorderLevel;

    private Boolean isScheduleDrug;
}
