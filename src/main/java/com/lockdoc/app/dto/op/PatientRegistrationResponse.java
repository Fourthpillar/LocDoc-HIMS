package com.lockdoc.app.dto.op;

import com.lockdoc.app.entity.op.PatientRegistration;
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
public class PatientRegistrationResponse {

    private Long id;
    private Long patientId;
    private String patientName;
    private String patientMrn;
    private String registrationNo;
    private Boolean isReRegistration;
    private LocalDate expiryDate;
    private LocalDateTime registeredAt;
    private BillResponse bill;

    public static PatientRegistrationResponse toResponse(PatientRegistration r, BillResponse bill) {
        return PatientRegistrationResponse.builder()
                .id(r.getId())
                .patientId(r.getPatient().getId())
                .patientName(r.getPatient().getFullName())
                .patientMrn(r.getPatient().getMrn())
                .registrationNo(r.getRegistrationNo())
                .isReRegistration(r.getIsReRegistration())
                .expiryDate(r.getExpiryDate())
                .registeredAt(r.getRegisteredAt())
                .bill(bill)
                .build();
    }
}
