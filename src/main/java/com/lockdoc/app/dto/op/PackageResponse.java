package com.lockdoc.app.dto.op;

import com.lockdoc.app.entity.op.Package;
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
public class PackageResponse {

    private Long id;
    private String name;
    private String code;
    private BigDecimal price;
    private Boolean active;
    private List<PackageItemResponse> items;

    public static PackageResponse toResponse(Package p, List<PackageItemResponse> items) {
        return PackageResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .code(p.getCode())
                .price(p.getPrice())
                .active(p.getActive())
                .items(items)
                .build();
    }
}
