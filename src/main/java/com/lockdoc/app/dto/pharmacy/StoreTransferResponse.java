package com.lockdoc.app.dto.pharmacy;

import com.lockdoc.app.entity.pharmacy.StoreTransfer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreTransferResponse {

    private Long id;
    private String transferNo;
    private Long fromStoreId;
    private String fromStoreName;
    private Long toStoreId;
    private String toStoreName;
    private String status;
    private LocalDateTime issuedDate;
    private LocalDateTime receivedDate;
    private String discrepancyNotes;
    private List<StoreTransferItemResponse> items;

    public static StoreTransferResponse toResponse(StoreTransfer t) {
        return StoreTransferResponse.builder()
                .id(t.getId())
                .transferNo(t.getTransferNo())
                .fromStoreId(t.getFromStore().getId())
                .fromStoreName(t.getFromStore().getName())
                .toStoreId(t.getToStore().getId())
                .toStoreName(t.getToStore().getName())
                .status(t.getStatus())
                .issuedDate(t.getIssuedDate())
                .receivedDate(t.getReceivedDate())
                .discrepancyNotes(t.getDiscrepancyNotes())
                .items(t.getItems().stream().map(StoreTransferItemResponse::toResponse).toList())
                .build();
    }
}
