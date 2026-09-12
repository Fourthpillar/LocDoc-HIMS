package com.lockdoc.app.dto.pharmacy;

import com.lockdoc.app.entity.pharmacy.StatutoryRegisterEntry;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatutoryRegisterEntryResponse {

    private Long id;
    private String registerType;
    private Long salesInvoiceItemId;
    private String medicineName;
    private String patientName;
    private Integer qty;
    private String prescriberName;
    private String prescriberRegistrationNumber;
    private LocalDate saleDate;
    private LocalDateTime createdDate;

    public static StatutoryRegisterEntryResponse toResponse(StatutoryRegisterEntry e) {
        return StatutoryRegisterEntryResponse.builder()
                .id(e.getId())
                .registerType(e.getRegisterType())
                .salesInvoiceItemId(e.getSalesInvoiceItemId())
                .medicineName(e.getMedicineName())
                .patientName(e.getPatientName())
                .qty(e.getQty())
                .prescriberName(e.getPrescriberName())
                .prescriberRegistrationNumber(e.getPrescriberRegistrationNumber())
                .saleDate(e.getSaleDate())
                .createdDate(e.getCreatedDate())
                .build();
    }
}
