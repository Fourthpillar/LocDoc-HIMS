package com.lockdoc.app.dto.pharmacy;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PatientRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    private String phone;

    private String gender;

    private LocalDate dateOfBirth;

    private String address;

    /** Free-text, comma-separated (Master Spec §6/§11.5/§17.6). */
    private String allergies;

    /** Area-wise consultations report (§17.7 #12a) - optional. */
    private Long areaId;
}
