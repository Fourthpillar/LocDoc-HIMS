package com.lockdoc.app.dto.pharmacy;

import com.lockdoc.app.entity.pharmacy.StoreTransferItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreTransferItemResponse {

    private Long id;
    private Long medicineId;
    private String medicineName;
    private String batchNo;
    private LocalDate expiryDate;
    private Integer issuedQty;
    private Integer receivedQty;

    public static StoreTransferItemResponse toResponse(StoreTransferItem i) {
        return StoreTransferItemResponse.builder()
                .id(i.getId())
                .medicineId(i.getMedicine().getId())
                .medicineName(i.getMedicine().getName())
                .batchNo(i.getBatchNo())
                .expiryDate(i.getExpiryDate())
                .issuedQty(i.getIssuedQty())
                .receivedQty(i.getReceivedQty())
                .build();
    }
}
