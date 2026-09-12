package com.lockdoc.common.dto;

import com.lockdoc.common.entity.Facility;
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
public class FacilityResponse {

    private Long id;
    private String name;
    private String code;
    private String type;
    private String city;
    private Boolean active;

    public static FacilityResponse toResponse(Facility f) {
        return FacilityResponse.builder()
                .id(f.getId())
                .name(f.getName())
                .code(f.getCode())
                .type(f.getType())
                .city(f.getCity())
                .active(f.getActive())
                .build();
    }
}
