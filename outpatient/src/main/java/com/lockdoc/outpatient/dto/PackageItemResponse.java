package com.lockdoc.outpatient.dto;

import com.lockdoc.outpatient.entity.PackageItem;
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
public class PackageItemResponse {

    private Long billableItemId;
    private String itemName;
    private String itemType;
    private Integer includedQty;

    public static PackageItemResponse toResponse(PackageItem i) {
        return PackageItemResponse.builder()
                .billableItemId(i.getBillableItem().getId())
                .itemName(i.getBillableItem().getName())
                .itemType(i.getBillableItem().getItemType())
                .includedQty(i.getIncludedQty())
                .build();
    }
}
