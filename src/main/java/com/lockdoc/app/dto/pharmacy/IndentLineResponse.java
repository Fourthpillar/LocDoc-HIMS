package com.lockdoc.app.dto.pharmacy;

import com.lockdoc.app.entity.pharmacy.IndentLine;
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
public class IndentLineResponse {

    private Long id;
    private Long medicineId;
    private String medicineName;
    private Integer requestedQty;
    private Integer approvedQty;
    private Integer issuedQty;

    public static IndentLineResponse toResponse(IndentLine l) {
        return IndentLineResponse.builder()
                .id(l.getId())
                .medicineId(l.getMedicine().getId())
                .medicineName(l.getMedicine().getName())
                .requestedQty(l.getRequestedQty())
                .approvedQty(l.getApprovedQty())
                .issuedQty(l.getIssuedQty())
                .build();
    }
}
