package com.lockdoc.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/** Master Spec §17.7 #33b - only the fields a facility may self-edit; type/verification/module-entitlement stay Super-Admin-only. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FacilityProfileRequest {

    @NotBlank(message = "name is required")
    private String name;

    private String address;
    private BigDecimal geoLat;
    private BigDecimal geoLng;
    private String licenceNumber;
}
