package com.lockdoc.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Self-registration (Master Spec §10): a doctor submits their own
 * credentials and profile in one step, matching how every other
 * self-serve signup in this codebase works. The account is created
 * disabled - Super Admin's verify step (DoctorService.verify) is what
 * actually enables login, not this request. Username/password are
 * collected here rather than generated at verification time, since this
 * codebase has no channel to deliver a system-generated credential to
 * the doctor once verified.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DoctorRegisterRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 10, message = "Password must be at least 10 characters")
    private String password;

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Registration number is required")
    private String registrationNumber;

    private String specialties;

    private String email;
}
