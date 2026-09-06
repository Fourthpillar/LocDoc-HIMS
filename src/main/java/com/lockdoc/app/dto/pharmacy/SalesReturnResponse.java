package com.lockdoc.app.dto.pharmacy;

import com.lockdoc.app.entity.pharmacy.SalesReturn;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesReturnResponse {

    private Long id;
    private String returnNumber;
    private Long salesInvoiceId;
    private String invoiceNumber;
    private LocalDate returnDate;
    private String reason;
    private BigDecimal totalAmount;
    private String status;
    private Long createdBy;
    private List<SalesReturnItemResponse> items;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    public static SalesReturnResponse toResponse(SalesReturn sr) {
        return SalesReturnResponse.builder()
                .id(sr.getId())
                .returnNumber(sr.getReturnNumber())
                .salesInvoiceId(sr.getSalesInvoice().getId())
                .invoiceNumber(sr.getSalesInvoice().getInvoiceNumber())
                .returnDate(sr.getReturnDate())
                .reason(sr.getReason())
                .totalAmount(sr.getTotalAmount())
                .status(sr.getStatus())
                .createdBy(sr.getCreatedBy())
                .items(sr.getItems().stream().map(SalesReturnItemResponse::toResponse).toList())
                .createdDate(sr.getCreatedDate())
                .updatedDate(sr.getUpdatedDate())
                .build();
    }
}
