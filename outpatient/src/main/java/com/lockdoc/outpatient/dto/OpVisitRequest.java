package com.lockdoc.outpatient.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/** Front-desk visit/vitals capture (Master Spec §7.4). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OpVisitRequest {

    @NotNull(message = "patientId is required")
    private Long patientId;

    @NotNull(message = "doctorId is required")
    private Long doctorId;

    /** Nullable - a walk-in with no prior appointment. */
    private Long appointmentId;

    private String orgType;
    private String visitType;

    private BigDecimal weightKg;
    private BigDecimal heightCm;
    private BigDecimal temperatureF;
    private String bp;

    private String attendantName;
    private String attendantMobile;
    private String attendantRelation;

    private Boolean mlcFlag;
    private String mlcPoliceStation;
    private String mlcNumber;

    /** Referral attribution (§7.4/V46) - optional, feeds the Commission report. */
    private Long referralDoctorId;
    private Long proId;
}
