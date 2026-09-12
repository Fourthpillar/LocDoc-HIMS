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
public class PharmacyConversionReportResponse {
    private int totalVisits;
    private int distinctPatients;
    private int patientsWithPharmacySale;
    private double conversionPercent;
}
