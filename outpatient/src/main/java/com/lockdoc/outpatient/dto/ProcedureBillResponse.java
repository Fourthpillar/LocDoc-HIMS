package com.lockdoc.outpatient.dto;

import com.lockdoc.outpatient.entity.ProcedureBill;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcedureBillResponse {

    private Long id;
    /** Null for an unregistered walk-in (V44) - see walkInName etc. instead. */
    private Long patientId;
    private String patientName;
    private String patientMrn;
    private String walkInName;
    private Integer walkInAge;
    private String walkInGender;
    private String walkInMobile;
    private Long billableItemId;
    private String itemName;
    private String itemType;
    private Long doctorId;
    private String doctorName;
    private String orgType;
    private String performedByName;
    private Long billId;
    private String billNo;
    private LocalDateTime createdDate;

    public static ProcedureBillResponse toResponse(ProcedureBill p, String billNo) {
        return ProcedureBillResponse.builder()
                .id(p.getId())
                .patientId(p.getPatient() != null ? p.getPatient().getId() : null)
                .patientName(p.getPatient() != null ? p.getPatient().getFullName() : null)
                .patientMrn(p.getPatient() != null ? p.getPatient().getMrn() : null)
                .walkInName(p.getWalkInName())
                .walkInAge(p.getWalkInAge())
                .walkInGender(p.getWalkInGender())
                .walkInMobile(p.getWalkInMobile())
                .billableItemId(p.getBillableItem().getId())
                .itemName(p.getBillableItem().getName())
                .itemType(p.getBillableItem().getItemType())
                .doctorId(p.getDoctor() != null ? p.getDoctor().getId() : null)
                .doctorName(p.getDoctor() != null ? p.getDoctor().getFullName() : null)
                .orgType(p.getOrgType())
                .performedByName(p.getPerformedByName())
                .billId(p.getBillId())
                .billNo(billNo)
                .createdDate(p.getCreatedDate())
                .build();
    }
}
