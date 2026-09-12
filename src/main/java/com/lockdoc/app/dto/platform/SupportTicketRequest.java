package com.lockdoc.app.dto.platform;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SupportTicketRequest {

    /** Required only when the caller has no single home facility of their own (a Doctor, who may practise at several) - a Hospital/Clinic Admin's own facility is used regardless of this field. */
    private Long facilityId;

    private String category;

    @NotBlank(message = "subject is required")
    private String subject;

    @NotBlank(message = "description is required")
    private String description;
}
