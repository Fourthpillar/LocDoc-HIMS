package com.lockdoc.doctor.dto;

/**
 * Just enough of a facility for a doctor to recognise the one they mean
 * when raising a request (Master Spec §8.1). Deliberately not
 * FacilityResponse: that carries licence numbers, verification state and
 * module entitlements, none of which a doctor outside the facility has any
 * business reading — this is the same "only what the picker needs" shape
 * DoctorSearchResult takes on the admin side.
 */
public record FacilitySearchResult(Long id, String name, String type, String address) {
}
