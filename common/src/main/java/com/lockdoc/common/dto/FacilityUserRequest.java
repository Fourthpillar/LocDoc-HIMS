package com.lockdoc.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Master Spec §17.7 #33a - creates a Receptionist account at the admin's own facility. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FacilityUserRequest {

    @NotBlank(message = "username is required")
    private String username;

    @NotBlank(message = "password is required")
    @Size(min = 10, message = "password must be at least 10 characters")
    private String password;

    @NotBlank(message = "fullName is required")
    private String fullName;

    private String email;

    @NotBlank(message = "roleCode is required")
    private String roleCode;
}
