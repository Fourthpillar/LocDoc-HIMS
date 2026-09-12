package com.lockdoc.app.dto.doctor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/** A verified doctor asking to practise at a facility (Master Spec §8.1) — the mirror of FacilityMappingInviteRequest. */
@Getter
@Setter
public class FacilityMappingRequestRequest {

    @NotNull(message = "facilityId is required")
    private Long facilityId;

    /**
     * What the doctor is proposing the relationship should be. The facility
     * confirms or declines it — nothing is settled until they respond, so
     * this is a proposal, not a claim.
     */
    private String relationshipType;

    /**
     * When the doctor is offering to consult here (§8.2). Optional — a doctor
     * who wants to settle the hours in conversation can still send the request
     * — but stating them is what lets the facility answer without a phone call,
     * and what the receptionist reads once the mapping is live.
     */
    @Valid
    private List<ConsultationHourRequest> consultationHours;
}
