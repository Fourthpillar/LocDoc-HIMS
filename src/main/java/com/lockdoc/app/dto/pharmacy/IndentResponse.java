package com.lockdoc.app.dto.pharmacy;

import com.lockdoc.app.entity.pharmacy.Indent;
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
public class IndentResponse {

    private Long id;
    private String indentNo;
    private Long fromStoreId;
    private String fromStoreName;
    private Long toStoreId;
    private String toStoreName;
    private String status;
    private String remarks;
    private Long requestedByUserId;
    private Long approvedByUserId;
    private LocalDateTime approvedDate;
    private List<IndentLineResponse> lines;
    private LocalDateTime createdDate;

    public static IndentResponse toResponse(Indent i) {
        return IndentResponse.builder()
                .id(i.getId())
                .indentNo(i.getIndentNo())
                .fromStoreId(i.getFromStore().getId())
                .fromStoreName(i.getFromStore().getName())
                .toStoreId(i.getToStore().getId())
                .toStoreName(i.getToStore().getName())
                .status(i.getStatus())
                .remarks(i.getRemarks())
                .requestedByUserId(i.getRequestedByUserId())
                .approvedByUserId(i.getApprovedByUserId())
                .approvedDate(i.getApprovedDate())
                .lines(i.getLines().stream().map(IndentLineResponse::toResponse).toList())
                .createdDate(i.getCreatedDate())
                .build();
    }
}
