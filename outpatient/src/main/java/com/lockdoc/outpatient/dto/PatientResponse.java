package com.lockdoc.outpatient.dto;

import com.lockdoc.outpatient.entity.Patient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientResponse {

    private Long id;
    private String mrn;
    private String fullName;
    private String phone;
    private String gender;
    private LocalDate dateOfBirth;
    private String address;
    private String allergies;
    private Long areaId;
    private String areaLabel;
    private Boolean active;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    public static PatientResponse toResponse(Patient p) {
        return PatientResponse.builder()
                .id(p.getId())
                .mrn(p.getMrn())
                .fullName(p.getFullName())
                .phone(p.getPhone())
                .gender(p.getGender())
                .dateOfBirth(p.getDateOfBirth())
                .address(p.getAddress())
                .allergies(p.getAllergies())
                .areaId(p.getArea() != null ? p.getArea().getId() : null)
                .areaLabel(p.getArea() != null ? p.getArea().displayLabel() : null)
                .active(p.getActive())
                .createdDate(p.getCreatedDate())
                .updatedDate(p.getUpdatedDate())
                .build();
    }
}
