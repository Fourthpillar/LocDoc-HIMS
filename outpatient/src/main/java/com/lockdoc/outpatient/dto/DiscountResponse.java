package com.lockdoc.outpatient.dto;

import com.lockdoc.outpatient.entity.Discount;
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
public class DiscountResponse {

    private Long id;
    private Long billId;
    private String discountKind;
    private BigDecimal value;
    private BigDecimal amount;
    private String reason;
    private String status;
    private Long requestedByUserId;
    private Long approvedByUserId;
    private LocalDateTime approvedAt;
    private LocalDateTime createdDate;

    public static DiscountResponse toResponse(Discount d) {
        return DiscountResponse.builder()
                .id(d.getId())
                .billId(d.getBill().getId())
                .discountKind(d.getDiscountKind())
                .value(d.getValue())
                .amount(d.getAmount())
                .reason(d.getReason())
                .status(d.getStatus())
                .requestedByUserId(d.getRequestedByUserId())
                .approvedByUserId(d.getApprovedByUserId())
                .approvedAt(d.getApprovedAt())
                .createdDate(d.getCreatedDate())
                .build();
    }
}
