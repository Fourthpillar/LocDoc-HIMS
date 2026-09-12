package com.lockdoc.app.dto;

import com.lockdoc.app.entity.Facility;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacilityResponse {

    private Long id;
    private String name;
    private String type;
    private String address;
    private BigDecimal geoLat;
    private BigDecimal geoLng;
    private String licenceNumber;
    private String verificationStatus;
    private Boolean active;
    private Set<String> activeModules;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    public static FacilityResponse toResponse(Facility f) {
        return FacilityResponse.builder()
                .id(f.getId())
                .name(f.getName())
                .type(f.getType())
                .address(f.getAddress())
                .geoLat(f.getGeoLat())
                .geoLng(f.getGeoLng())
                .licenceNumber(f.getLicenceNumber())
                .verificationStatus(f.getVerificationStatus())
                .active(f.getActive())
                .activeModules(f.getActiveModules())
                .createdDate(f.getCreatedDate())
                .updatedDate(f.getUpdatedDate())
                .build();
    }
}
