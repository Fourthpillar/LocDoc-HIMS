package com.lockdoc.outpatient.dto;

import com.lockdoc.outpatient.entity.CounterSession;
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
public class CounterSessionResponse {

    private Long id;
    private String counterType;
    private String status;
    private Long openedByUserId;
    private LocalDateTime openedAt;
    private Long closedByUserId;
    private LocalDateTime closedAt;
    private BigDecimal declaredCash;
    private BigDecimal systemTotal;
    private BigDecimal variance;

    public static CounterSessionResponse toResponse(CounterSession c) {
        return CounterSessionResponse.builder()
                .id(c.getId()).counterType(c.getCounterType()).status(c.getStatus())
                .openedByUserId(c.getOpenedByUserId()).openedAt(c.getOpenedAt())
                .closedByUserId(c.getClosedByUserId()).closedAt(c.getClosedAt())
                .declaredCash(c.getDeclaredCash()).systemTotal(c.getSystemTotal()).variance(c.getVariance())
                .build();
    }
}
