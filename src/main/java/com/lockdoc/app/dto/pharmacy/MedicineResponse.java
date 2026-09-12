package com.lockdoc.app.dto.pharmacy;

import com.lockdoc.app.entity.pharmacy.Medicine;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicineResponse {

    private Long id;
    private String code;
    private String name;
    private String genericName;
    private String manufacturer;
    private String category;
    private String uom;
    private String hsnCode;
    private BigDecimal taxPercent;
    private Integer reorderLevel;
    private Boolean isScheduleDrug;
    private String drugSchedule;
    private Boolean highAlert;
    private Boolean active;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    public static MedicineResponse toResponse(Medicine m) {
        return MedicineResponse.builder()
                .id(m.getId())
                .code(m.getCode())
                .name(m.getName())
                .genericName(m.getGenericName())
                .manufacturer(m.getManufacturer())
                .category(m.getCategory())
                .uom(m.getUom())
                .hsnCode(m.getHsnCode())
                .taxPercent(m.getTaxPercent())
                .reorderLevel(m.getReorderLevel())
                .isScheduleDrug(m.getIsScheduleDrug())
                .drugSchedule(m.getDrugSchedule())
                .highAlert(m.getHighAlert())
                .active(m.getActive())
                .createdDate(m.getCreatedDate())
                .updatedDate(m.getUpdatedDate())
                .build();
    }
}
