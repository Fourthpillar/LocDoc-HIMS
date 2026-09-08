package com.lockdoc.app.dto.pharmacy;

import com.lockdoc.app.entity.pharmacy.SalesInvoice;
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
public class SalesInvoiceResponse {

    private Long id;
    private String invoiceNumber;
    private Long patientId;
    private String patientName;
    private String walkInCustomerName;
    private String walkInCustomerPhone;
    private LocalDate saleDate;
    private String paymentMode;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private BigDecimal amountPaid;
    private BigDecimal balanceDue;
    private BigDecimal roundOffAmount;
    private String status;
    private Long createdBy;
    private List<SalesInvoiceItemResponse> items;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    public static SalesInvoiceResponse toResponse(SalesInvoice s) {
        return SalesInvoiceResponse.builder()
                .id(s.getId())
                .invoiceNumber(s.getInvoiceNumber())
                .patientId(s.getPatient() != null ? s.getPatient().getId() : null)
                .patientName(s.getPatient() != null ? s.getPatient().getFullName() : null)
                .walkInCustomerName(s.getWalkInCustomerName())
                .walkInCustomerPhone(s.getWalkInCustomerPhone())
                .saleDate(s.getSaleDate())
                .paymentMode(s.getPaymentMode())
                .subtotal(s.getSubtotal())
                .taxAmount(s.getTaxAmount())
                .discountAmount(s.getDiscountAmount())
                .totalAmount(s.getTotalAmount())
                .amountPaid(s.getAmountPaid())
                .balanceDue(s.getBalanceDue())
                .roundOffAmount(s.getRoundOffAmount())
                .status(s.getStatus())
                .createdBy(s.getCreatedBy())
                .items(s.getItems().stream().map(SalesInvoiceItemResponse::toResponse).toList())
                .createdDate(s.getCreatedDate())
                .updatedDate(s.getUpdatedDate())
                .build();
    }
}
