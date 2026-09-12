package com.lockdoc.app.dto.op;

import com.lockdoc.app.entity.op.Bill;
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
public class BillResponse {

    private Long id;
    private Long facilityId;
    /** Null for an unregistered walk-in procedure bill (V44) - see ProcedureBillResponse's own walkInName for who it actually was. */
    private Long patientId;
    private String patientName;
    private String patientMrn;
    private String billNo;
    private String encounterType;
    private Long encounterId;
    private BigDecimal gross;
    private BigDecimal discount;
    private BigDecimal tax;
    private BigDecimal net;
    private BigDecimal paid;
    private BigDecimal due;
    private String status;
    /** NONE / PENDING / REFUNDED - only meaningful once status is CANCELLED and something had been paid. */
    private String refundStatus;
    private LocalDateTime createdDate;

    public static BillResponse toResponse(Bill b) {
        return BillResponse.builder()
                .id(b.getId())
                .facilityId(b.getFacility().getId())
                .patientId(b.getPatient() != null ? b.getPatient().getId() : null)
                .patientName(b.getPatient() != null ? b.getPatient().getFullName() : null)
                .patientMrn(b.getPatient() != null ? b.getPatient().getMrn() : null)
                .billNo(b.getBillNo())
                .encounterType(b.getEncounterType())
                .encounterId(b.getEncounterId())
                .gross(b.getGross())
                .discount(b.getDiscount())
                .tax(b.getTax())
                .net(b.getNet())
                .paid(b.getPaid())
                .due(b.getDue())
                .status(b.getStatus())
                .refundStatus(b.getRefundStatus())
                .createdDate(b.getCreatedDate())
                .build();
    }
}
