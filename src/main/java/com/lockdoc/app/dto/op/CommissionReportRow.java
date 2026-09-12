package com.lockdoc.app.dto.op;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommissionReportRow {
    private String partyType;
    private String partyName;
    private int visitCount;
    private BigDecimal totalBilled;
    /** Null when no active CommissionBasis matches this party - "computed, not fabricated" (§8.1). */
    private String basis;
    private BigDecimal basisValue;
    private BigDecimal commissionAmount;
}
