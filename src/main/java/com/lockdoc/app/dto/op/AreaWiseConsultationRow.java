package com.lockdoc.app.dto.op;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AreaWiseConsultationRow {
    /** "Unspecified" when the patient has no area set (§17.7 #12a, V47 - nullable, never force-backfilled). */
    private String areaLabel;
    private int visitCount;
    private int distinctPatients;
}
