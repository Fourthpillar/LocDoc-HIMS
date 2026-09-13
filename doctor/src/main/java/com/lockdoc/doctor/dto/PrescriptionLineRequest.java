package com.lockdoc.doctor.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionLineRequest {

    @NotBlank(message = "medicineName is required")
    private String medicineName;

    private String genericName;
    private String strength;
    private String dosage;
    private String route;
    private String frequency;
    private String duration;
    private Integer quantity;
    private Boolean refillFlag;
}
