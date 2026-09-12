package com.lockdoc.app.dto.doctor;

import com.lockdoc.app.entity.doctor.PrescriptionLine;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code stockStatus} is one of IN_STOCK / LOW_STOCK / OUT_OF_STOCK /
 * PHARMACY_NOT_ACTIVE — never blank (Master Spec §8.7's module-
 * independence requirement: "Pharmacy not active at this facility",
 * never a blank or a zero, when the module isn't active there).
 */
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
    private Long matchedPharmacyMedicineId;
    private String stockStatus;
    private Integer availableQty;

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
                .matchedPharmacyMedicineId(l.getMatchedPharmacyMedicineId())
                .build();
    }
}
