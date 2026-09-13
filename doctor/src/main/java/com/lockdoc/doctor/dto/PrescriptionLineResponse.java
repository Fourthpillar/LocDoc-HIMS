package com.lockdoc.doctor.dto;

import com.lockdoc.doctor.entity.PrescriptionLine;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrescriptionLineResponse {

    private Long id;
    private String medicineName;
    private String genericName;
    private String strength;
    private String dosage;
    private String route;
    private String frequency;
    private String duration;
    private Integer quantity;
    private Boolean refillFlag;

    public static PrescriptionLineResponse toResponse(PrescriptionLine l) {
        return PrescriptionLineResponse.builder()
                .id(l.getId())
                .medicineName(l.getMedicineName())
                .genericName(l.getGenericName())
                .strength(l.getStrength())
                .dosage(l.getDosage())
                .route(l.getRoute())
                .frequency(l.getFrequency())
                .duration(l.getDuration())
                .quantity(l.getQuantity())
                .refillFlag(l.getRefillFlag())
                .build();
    }
}
