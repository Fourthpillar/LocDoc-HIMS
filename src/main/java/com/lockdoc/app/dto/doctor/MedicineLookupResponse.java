package com.lockdoc.app.dto.doctor;

import com.lockdoc.app.dto.pharmacy.MedicineStockResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * {@code pharmacyActive=false} is the module-independence sentinel
 * (Master Spec §8.7/§5.2) — the frontend renders "Pharmacy not active at
 * this facility" from this flag, never inferring it from an empty
 * results list, which would be indistinguishable from "no matches".
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicineLookupResponse {

    private boolean pharmacyActive;
    private List<MedicineStockResponse> results;
}
