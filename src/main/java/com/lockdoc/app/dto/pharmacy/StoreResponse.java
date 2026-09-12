package com.lockdoc.app.dto.pharmacy;

import com.lockdoc.app.entity.Store;
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
public class StoreResponse {

    private Long id;
    private Long facilityId;
    private String name;
    private String code;
    private Boolean active;

    public static StoreResponse toResponse(Store s) {
        return StoreResponse.builder()
                .id(s.getId())
                .facilityId(s.getFacility().getId())
                .name(s.getName())
                .code(s.getCode())
                .active(s.getActive())
                .build();
    }
}
