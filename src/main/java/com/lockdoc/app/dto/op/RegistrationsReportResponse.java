package com.lockdoc.app.dto.op;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationsReportResponse {
    private List<RegistrationsReportRow> rows;
    private int totalCount;
    private int reRegistrationCount;
    private BigDecimal totalNet;
    private BigDecimal totalCollected;
}
