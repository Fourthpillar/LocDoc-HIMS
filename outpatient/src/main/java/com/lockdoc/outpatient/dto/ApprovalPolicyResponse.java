package com.lockdoc.outpatient.dto;

import com.lockdoc.outpatient.entity.ApprovalPolicy;
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
public class ApprovalPolicyResponse {

    private Long id;
    private String approvalType;
    private BigDecimal thresholdValue;

    public static ApprovalPolicyResponse toResponse(ApprovalPolicy p) {
        return ApprovalPolicyResponse.builder()
                .id(p.getId())
                .approvalType(p.getApprovalType())
                .thresholdValue(p.getThresholdValue())
                .build();
    }
}
