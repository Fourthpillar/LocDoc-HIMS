package com.lockdoc.doctor.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Only what a doctor may self-edit - registrationNumber/verificationStatus stay Super-Admin-owned (Master Spec §8.9). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DoctorProfileUpdateRequest {

    private String specialties;
    private String qualifications;
}
