package com.lockdoc.outpatient.dto;

import com.lockdoc.outpatient.entity.CommissionBasis;
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
public class CommissionBasisResponse {

    private Long id;
    private String partyType;
    private String partyName;
    private String basis;
    private BigDecimal value;
    private String appliesTo;
    private Boolean active;
    private LocalDateTime updatedDate;

    public static CommissionBasisResponse toResponse(CommissionBasis c) {
        return CommissionBasisResponse.builder()
                .id(c.getId())
                .partyType(c.getPartyType())
                .partyName(c.getPartyName())
                .basis(c.getBasis())
                .value(c.getValue())
                .appliesTo(c.getAppliesTo())
                .active(c.getActive())
                .updatedDate(c.getUpdatedDate())
                .build();
    }
}
