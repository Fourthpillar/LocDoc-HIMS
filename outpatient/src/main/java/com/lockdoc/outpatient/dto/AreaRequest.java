package com.lockdoc.outpatient.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AreaRequest {

    @NotBlank(message = "country is required")
    private String country;

    @NotBlank(message = "state is required")
    private String state;

    @NotBlank(message = "city is required")
    private String city;

    @NotBlank(message = "areaName is required")
    private String areaName;
}
