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
public class DiscountsReportRow {
    private String billNo;
    private String patientName;
    private String patientMrn;
    private String discountKind;
    private BigDecimal value;
    private BigDecimal amount;
    private String reason;
    private String status;
    private LocalDateTime createdDate;
}
