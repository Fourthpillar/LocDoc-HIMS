package com.lockdoc.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/** Master Spec §8.9, screen #19 - "qualifications, centrally-verified registration/council number, NMC/ABDM verification badge... specialties, facilities practised at." */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorProfileResponse {

    private Long id;
    private String fullName;
    private String registrationNumber;
    private String verificationStatus;
    private String specialties;
    private String qualifications;
    private String username;
    private String email;
    private List<String> facilitiesPractisedAt;
}
