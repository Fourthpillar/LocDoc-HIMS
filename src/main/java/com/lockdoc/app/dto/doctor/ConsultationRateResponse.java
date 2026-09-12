package com.lockdoc.app.dto.doctor;

import com.lockdoc.app.entity.doctor.ConsultationRate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationRateResponse {

    private Long id;
    private Long doctorId;
    private Long facilityId;
    private String facilityName;
    private String orgType;
    private String dayNightIndicator;
    private BigDecimal totalAmount;
    private BigDecimal hospitalPercent;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private String status;
    private Long proposedByUserId;
    private Long approvedByUserId;
    private LocalDateTime approvedAt;
    private String rejectionReason;
    private LocalDateTime createdDate;

    public static ConsultationRateResponse toResponse(ConsultationRate r) {
        return ConsultationRateResponse.builder()
                .id(r.getId())
                .doctorId(r.getDoctor().getId())
                .facilityId(r.getFacility().getId())
                .facilityName(r.getFacility().getName())
                .orgType(r.getOrgType())
                .dayNightIndicator(r.getDayNightIndicator())
                .totalAmount(r.getTotalAmount())
                .hospitalPercent(r.getHospitalPercent())
                .effectiveFrom(r.getEffectiveFrom())
                .effectiveTo(r.getEffectiveTo())
                .status(r.getStatus())
                .proposedByUserId(r.getProposedByUserId())
                .approvedByUserId(r.getApprovedByUserId())
                .approvedAt(r.getApprovedAt())
                .rejectionReason(r.getRejectionReason())
                .createdDate(r.getCreatedDate())
                .build();
    }
}
