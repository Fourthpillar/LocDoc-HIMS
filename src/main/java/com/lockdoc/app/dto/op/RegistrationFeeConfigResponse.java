package com.lockdoc.app.dto.op;

import com.lockdoc.app.entity.op.RegistrationFeeConfig;
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
public class RegistrationFeeConfigResponse {

    private Long id;
    private Long facilityId;
    private BigDecimal firstFee;
    private BigDecimal reRegistrationFee;
    private Integer validityDays;
    private LocalDateTime updatedDate;

    public static RegistrationFeeConfigResponse toResponse(RegistrationFeeConfig c) {
        return RegistrationFeeConfigResponse.builder()
                .id(c.getId())
                .facilityId(c.getFacility().getId())
                .firstFee(c.getFirstFee())
                .reRegistrationFee(c.getReRegistrationFee())
                .validityDays(c.getValidityDays())
                .updatedDate(c.getUpdatedDate())
                .build();
    }
}
