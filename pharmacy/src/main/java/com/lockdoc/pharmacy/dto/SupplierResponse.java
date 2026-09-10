package com.lockdoc.pharmacy.dto;

import com.lockdoc.pharmacy.entity.Supplier;
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
public class SupplierResponse {

    private Long id;
    private String name;
    private String contactPerson;
    private String phone;
    private String email;
    private String address;
    private String gstin;
    private String mobile2;
    private String landline;
    private String city;
    private String state;
    private String pincode;
    private String tinNo;
    private String website;
    private String supplierType;
    private String drugLicenseNo;
    private Boolean active;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    public static SupplierResponse toResponse(Supplier s) {
        return SupplierResponse.builder()
                .id(s.getId())
                .name(s.getName())
                .contactPerson(s.getContactPerson())
                .phone(s.getPhone())
                .email(s.getEmail())
                .address(s.getAddress())
                .gstin(s.getGstin())
                .mobile2(s.getMobile2())
                .landline(s.getLandline())
                .city(s.getCity())
                .state(s.getState())
                .pincode(s.getPincode())
                .tinNo(s.getTinNo())
                .website(s.getWebsite())
                .supplierType(s.getSupplierType())
                .drugLicenseNo(s.getDrugLicenseNo())
                .active(s.getActive())
                .createdDate(s.getCreatedDate())
                .updatedDate(s.getUpdatedDate())
                .build();
    }
}
