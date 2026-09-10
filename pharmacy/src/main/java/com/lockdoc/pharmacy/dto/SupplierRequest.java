package com.lockdoc.pharmacy.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SupplierRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String contactPerson;

    private String phone;

    @Email(message = "Email must be valid")
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
}
