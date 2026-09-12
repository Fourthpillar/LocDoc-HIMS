package com.lockdoc.app.dto.pharmacy;

import com.lockdoc.app.entity.pharmacy.StockCount;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockCountResponse {

    private Long id;
    private String countNo;
    private Long storeId;
    private String storeName;
    private String status;
    private LocalDate countDate;
    private List<StockCountLineResponse> lines;
    private LocalDateTime createdDate;

    public static StockCountResponse toResponse(StockCount c) {
        // system_qty/variance only revealed once APPROVED - "blind" while counting is in progress (§11.4).
        boolean reveal = StockCount.STATUS_APPROVED.equals(c.getStatus());
        return StockCountResponse.builder()
                .id(c.getId())
                .countNo(c.getCountNo())
                .storeId(c.getStore().getId())
                .storeName(c.getStore().getName())
                .status(c.getStatus())
                .countDate(c.getCountDate())
                .lines(c.getLines().stream().map(l -> StockCountLineResponse.toResponse(l, reveal)).toList())
                .createdDate(c.getCreatedDate())
                .build();
    }
}
