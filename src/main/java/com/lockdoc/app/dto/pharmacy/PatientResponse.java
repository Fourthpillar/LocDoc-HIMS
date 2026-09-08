package com.lockdoc.app.dto.pharmacy;

import com.lockdoc.app.entity.pharmacy.Patient;
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
                .active(p.getActive())
                .createdDate(p.getCreatedDate())
                .updatedDate(p.getUpdatedDate())
                .build();
    }
}
