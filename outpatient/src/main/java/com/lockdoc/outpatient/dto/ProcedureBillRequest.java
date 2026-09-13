package com.lockdoc.outpatient.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProcedureBillRequest {

    /** Registered patient path - omit and set walkInName instead for an unregistered walk-in (§7.5, V44). Exactly one of the two is required, enforced in ProcedureBillingService. */
    private Long patientId;

    /** Unregistered walk-in path (§7.5) - name/age/gender/mobile captured inline, no patient_id at all. Name is the only one actually required. */
    private String walkInName;
    private Integer walkInAge;
    private String walkInGender;
    private String walkInMobile;

    private Long billableItemId;

    /** Optional per §7.5 - a procedure isn't tied to a doctor_id at all. */
    private Long doctorId;

    /** Defaults to DIRECT when omitted. */
    private String orgType;

    /** Free text - which staff member performed it, when the facility wants that recorded. */
    private String performedByName;
}
