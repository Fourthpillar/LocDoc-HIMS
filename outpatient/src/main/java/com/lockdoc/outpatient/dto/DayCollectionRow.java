package com.lockdoc.outpatient.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DayCollectionRow {
    private LocalDate date;
    private BigDecimal cash;
    private BigDecimal cardUpi;
    private BigDecimal cheque;
    private BigDecimal total;
}
