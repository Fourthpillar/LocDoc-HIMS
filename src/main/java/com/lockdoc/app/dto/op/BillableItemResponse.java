package com.lockdoc.app.dto.op;

import com.lockdoc.app.entity.op.BillableItem;
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
public class BillableItemResponse {

    private Long id;
    private String itemType;
    private String name;
    private String code;
    private BigDecimal rateDirect;
    private BigDecimal rateOrganization;
    private BigDecimal rateTpa;
    private Boolean active;

    public static BillableItemResponse toResponse(BillableItem b) {
        return BillableItemResponse.builder()
                .id(b.getId())
                .itemType(b.getItemType())
                .name(b.getName())
                .code(b.getCode())
                .rateDirect(b.getRateDirect())
                .rateOrganization(b.getRateOrganization())
                .rateTpa(b.getRateTpa())
                .active(b.getActive())
                .build();
    }
}
