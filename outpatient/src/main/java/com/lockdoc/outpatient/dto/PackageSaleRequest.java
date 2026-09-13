package com.lockdoc.outpatient.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PackageSaleRequest {

    @NotNull(message = "patientId is required")
    private Long patientId;

    @NotNull(message = "packageId is required")
    private Long packageId;
}
