package com.lockdoc.app.dto.op;

import com.lockdoc.app.entity.op.PackageUtilization;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackageUtilizationResponse {

    private Long id;
    private Long billableItemId;
    private String itemName;
    private String itemType;
    private Integer includedQty;
    private Integer usedQty;
    private Integer remainingQty;

    public static PackageUtilizationResponse toResponse(PackageUtilization u) {
        return PackageUtilizationResponse.builder()
                .id(u.getId())
                .billableItemId(u.getBillableItem().getId())
                .itemName(u.getBillableItem().getName())
                .itemType(u.getBillableItem().getItemType())
                .includedQty(u.getIncludedQty())
                .usedQty(u.getUsedQty())
                .remainingQty(u.getRemainingQty())
                .build();
    }
}
