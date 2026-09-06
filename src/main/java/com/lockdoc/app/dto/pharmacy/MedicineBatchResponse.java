package com.lockdoc.app.dto.pharmacy;

import com.lockdoc.app.entity.pharmacy.MedicineBatch;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicineBatchResponse {

    private Long id;
    private Long medicineId;
    private String medicineName;
    private String medicineCode;
    private String batchNo;
    private LocalDate expiryDate;
    private BigDecimal mrp;
    private BigDecimal purchaseRate;
    private BigDecimal saleRate;
    private Integer quantityOnHand;
    private Long supplierId;

    public static MedicineBatchResponse toResponse(MedicineBatch b) {
        return MedicineBatchResponse.builder()
                .id(b.getId())
                .medicineId(b.getMedicine().getId())
                .medicineName(b.getMedicine().getName())
                .medicineCode(b.getMedicine().getCode())
                .batchNo(b.getBatchNo())
                .expiryDate(b.getExpiryDate())
                .mrp(b.getMrp())
                .purchaseRate(b.getPurchaseRate())
                .saleRate(b.getSaleRate())
                .quantityOnHand(b.getQuantityOnHand())
                .supplierId(b.getSupplier() != null ? b.getSupplier().getId() : null)
                .build();
    }
}
