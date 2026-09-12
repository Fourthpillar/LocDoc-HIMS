package com.lockdoc.doctor.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FreeReviewPolicyRequest {

    @NotNull(message = "maxDays is required")
    @Min(value = 1, message = "maxDays must be at least 1")
    private Integer maxDays;

    @NotNull(message = "maxVisits is required")
    @Min(value = 1, message = "maxVisits must be at least 1")
    private Integer maxVisits;
}
