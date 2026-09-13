package com.lockdoc.outpatient.dto;

import com.lockdoc.outpatient.entity.Payment;
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
public class PaymentResponse {

    private Long id;
    private Long billId;
    private String partyType;
    private String paymentType;
    private BigDecimal amount;
    private LocalDateTime paidAt;
    private Long receivedByUserId;

    public static PaymentResponse toResponse(Payment p) {
        return PaymentResponse.builder()
                .id(p.getId())
                .billId(p.getBill().getId())
                .partyType(p.getPartyType())
                .paymentType(p.getPaymentType())
                .amount(p.getAmount())
                .paidAt(p.getPaidAt())
                .receivedByUserId(p.getReceivedByUserId())
                .build();
    }
}
