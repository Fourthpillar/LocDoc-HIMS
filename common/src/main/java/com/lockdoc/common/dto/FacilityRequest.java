package com.lockdoc.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * A facility self-registers with this shape (Master Spec §10: "Facility
 * registers and submits licence/registration details") before Super Admin
 * verifies it - registration and verification are deliberately two
 * separate steps/rights (FACILITY_MANAGE vs FACILITY_VERIFY), matching the
 * PENDING -> VERIFIED state on Facility.verificationStatus.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FacilityRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Type is required")
    private String type;

    private String address;

    private BigDecimal geoLat;

    private BigDecimal geoLng;

    private String licenceNumber;
}
