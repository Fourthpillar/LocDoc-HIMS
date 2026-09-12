package com.lockdoc.app.dto.op;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConsultationRatingRequest {

    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;

    /** Optional — what the patient told the front desk, verbatim or summarized. */
    private String comment;
}
