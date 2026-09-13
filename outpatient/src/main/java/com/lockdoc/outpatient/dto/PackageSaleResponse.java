package com.lockdoc.outpatient.dto;

import com.lockdoc.outpatient.entity.PackageSale;
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
public class PackageSaleResponse {

    private Long id;
    private Long patientId;
    private String patientName;
    private String patientMrn;
    private Long packageId;
    private String packageName;
    private Long billId;
    private String billNo;
    private LocalDateTime createdDate;
    private List<PackageUtilizationResponse> utilization;

    public static PackageSaleResponse toResponse(PackageSale s, String billNo, List<PackageUtilizationResponse> utilization) {
        return PackageSaleResponse.builder()
                .id(s.getId())
                .patientId(s.getPatient().getId())
                .patientName(s.getPatient().getFullName())
                .patientMrn(s.getPatient().getMrn())
                .packageId(s.getPackageEntity().getId())
                .packageName(s.getPackageEntity().getName())
                .billId(s.getBillId())
                .billNo(billNo)
                .createdDate(s.getCreatedDate())
                .utilization(utilization)
                .build();
    }
}
