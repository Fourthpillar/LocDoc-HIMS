package com.lockdoc.app.dto.op;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReferralDoctorRequest {

    @NotBlank(message = "name is required")
    private String name;

    private String contact;
    private String registrationNo;
}
