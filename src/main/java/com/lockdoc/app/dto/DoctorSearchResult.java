package com.lockdoc.app.dto;

/** Minimal doctor lookup for the Hospital/Clinic Admin add-doctor flow (Master Spec §17.7 #15) - never the full Doctor Verification shape Super Admin sees. */
public record DoctorSearchResult(Long id, String fullName, String registrationNumber, String specialties) {
}
