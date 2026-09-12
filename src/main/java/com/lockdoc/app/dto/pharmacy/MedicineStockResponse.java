package com.lockdoc.app.dto.pharmacy;

import com.lockdoc.app.entity.pharmacy.Medicine;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A medicine paired with its aggregated, non-expired stock quantity —
 * used both by the Doctor Module's prescription stock-availability chip
 * (§8.7) and Pharmacy's generic-substitution surface (§11.5). One shape,
 * two callers, since it's the same underlying question either way:
 * "how much of this is actually available right now."
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicineStockResponse {

    private Long id;
    private String name;
    private String genericName;
    private String manufacturer;
    private String drugSchedule;
    private Boolean highAlert;
    private int availableQty;

    public static MedicineStockResponse of(Medicine m, int availableQty) {
        return MedicineStockResponse.builder()
                .id(m.getId())
                .name(m.getName())
                .genericName(m.getGenericName())
                .manufacturer(m.getManufacturer())
                .drugSchedule(m.getDrugSchedule())
                .highAlert(m.getHighAlert())
                .availableQty(availableQty)
                .build();
    }
}
