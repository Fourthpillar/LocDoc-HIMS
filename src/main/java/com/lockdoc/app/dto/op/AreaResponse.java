package com.lockdoc.app.dto.op;

import com.lockdoc.app.entity.op.Area;
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
public class AreaResponse {

    private Long id;
    private String country;
    private String state;
    private String city;
    private String areaName;
    private Boolean active;

    public static AreaResponse toResponse(Area a) {
        return AreaResponse.builder()
                .id(a.getId())
                .country(a.getCountry())
                .state(a.getState())
                .city(a.getCity())
                .areaName(a.getAreaName())
                .active(a.getActive())
                .build();
    }
}
