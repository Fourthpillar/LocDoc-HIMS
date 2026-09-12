package com.lockdoc.app.dto.op;

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
public class RegistrationsReportRow {
    private String patientName;
    private String patientMrn;
    private String registrationNo;
    private boolean reRegistration;
    private LocalDateTime registeredAt;
    private BigDecimal gross;
    private BigDecimal discount;
    private BigDecimal net;
    private BigDecimal paid;
    private BigDecimal due;
}
