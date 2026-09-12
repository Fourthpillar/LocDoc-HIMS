package com.lockdoc.app.dto.op;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * What this patient will owe if they are seen by this doctor (Master Spec
 * §7.2/§7.4/§8.6) — answered before the appointment is booked rather than
 * discovered at the counter afterwards.
 *
 * Three facts decide it, and all three are invisible to the person booking:
 * whether the patient's registration still holds, whether the doctor has an
 * approved rate at all, and whether this visit falls inside the doctor's
 * free-review window — in which case the consultation is ₹0 and the patient
 * should be told so while they are still on the phone.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VisitChargePreviewResponse {

    /** True when they'd have to register (or re-register) before they could be checked in. */
    private boolean registrationRequired;
    private BigDecimal registrationFee;
    private String registrationNote;

    private BigDecimal consultationFee;
    /** True when the doctor's free-review policy covers this visit — the fee is ₹0. */
    private boolean consultationFree;
    private String consultationNote;

    /** registrationFee + consultationFee, as the desk would quote it. */
    private BigDecimal total;
}
