package com.lockdoc.app.dto;

import com.lockdoc.app.entity.Doctor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorResponse {

    private Long id;
    private Long userId;
    private String username;
    private String fullName;
    private String registrationNumber;
    private String verificationStatus;
    private String specialties;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    public static DoctorResponse toResponse(Doctor d) {
        return DoctorResponse.builder()
                .id(d.getId())
                .userId(d.getUser().getId())
                .username(d.getUser().getUsername())
                .fullName(d.getFullName())
                .registrationNumber(d.getRegistrationNumber())
                .verificationStatus(d.getVerificationStatus())
                .specialties(d.getSpecialties())
                .createdDate(d.getCreatedDate())
                .updatedDate(d.getUpdatedDate())
                .build();
    }
}
