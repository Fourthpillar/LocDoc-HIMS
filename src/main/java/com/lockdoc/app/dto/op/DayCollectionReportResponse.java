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
public class DayCollectionReportResponse {
    private List<DayCollectionRow> rows;
    private BigDecimal grandTotal;
}
