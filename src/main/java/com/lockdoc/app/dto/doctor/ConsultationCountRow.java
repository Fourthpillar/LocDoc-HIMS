package com.lockdoc.app.dto.doctor;

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
public class ConsultationCountRow {
    private LocalDateTime consultedAt;
    private String facilityName;
    private String patientName;
    private String patientMrn;
    private String visitType;
    /** This visit's CONSULTATION bill net amount — null if it was never billed (e.g. bill not yet raised) or the bill was cancelled. */
    private BigDecimal billedAmount;
    /** 1-5 stars the front desk captured for this visit — null if it hasn't been rated. */
    private Integer rating;
}
