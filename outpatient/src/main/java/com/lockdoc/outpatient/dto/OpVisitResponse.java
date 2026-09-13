package com.lockdoc.outpatient.dto;

import com.lockdoc.outpatient.entity.OpVisit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpVisitResponse {

    private Long id;
    private String opNo;
    private Long facilityId;
    private Long patientId;
    private String patientName;
    private String patientMrn;
    private Long appointmentId;
    private Long doctorId;
    private String doctorName;
    private String orgType;
    private String visitType;
    private BigDecimal weightKg;
    private BigDecimal heightCm;
    private BigDecimal temperatureF;
    private String bp;
    private String attendantName;
    private String attendantMobile;
    private String attendantRelation;
    private String status;
    private Boolean mlcFlag;
    private String mlcPoliceStation;
    private String mlcNumber;
    private LocalDateTime arrivedTs;
    private LocalDateTime consultStartTs;
    private LocalDateTime consultEndTs;
    private Long referralDoctorId;
    private String referralDoctorName;
    private Long proId;
    private String proName;

    public static OpVisitResponse toResponse(OpVisit v) {
        return OpVisitResponse.builder()
                .id(v.getId())
                .opNo(v.getOpNo())
                .facilityId(v.getFacility().getId())
                .patientId(v.getPatient().getId())
                .patientName(v.getPatient().getFullName())
                .patientMrn(v.getPatient().getMrn())
                .appointmentId(v.getAppointmentId())
                .doctorId(v.getDoctor().getId())
                .doctorName(v.getDoctor().getFullName())
                .orgType(v.getOrgType())
                .visitType(v.getVisitType())
                .weightKg(v.getWeightKg())
                .heightCm(v.getHeightCm())
                .temperatureF(v.getTemperatureF())
                .bp(v.getBp())
                .attendantName(v.getAttendantName())
                .attendantMobile(v.getAttendantMobile())
                .attendantRelation(v.getAttendantRelation())
                .status(v.getStatus())
                .mlcFlag(v.getMlcFlag())
                .mlcPoliceStation(v.getMlcPoliceStation())
                .mlcNumber(v.getMlcNumber())
                .arrivedTs(v.getArrivedTs())
                .consultStartTs(v.getConsultStartTs())
                .consultEndTs(v.getConsultEndTs())
                .referralDoctorId(v.getReferralDoctor() != null ? v.getReferralDoctor().getId() : null)
                .referralDoctorName(v.getReferralDoctor() != null ? v.getReferralDoctor().getName() : null)
                .proId(v.getPro() != null ? v.getPro().getId() : null)
                .proName(v.getPro() != null ? v.getPro().getName() : null)
                .build();
    }
}
