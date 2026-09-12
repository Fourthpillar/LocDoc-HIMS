package com.lockdoc.app.dto.op;

import com.lockdoc.app.entity.op.ReferralDoctor;
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
public class ReferralDoctorResponse {

    private Long id;
    private String name;
    private String contact;
    private String registrationNo;
    private Boolean active;

    public static ReferralDoctorResponse toResponse(ReferralDoctor r) {
        return ReferralDoctorResponse.builder()
                .id(r.getId()).name(r.getName()).contact(r.getContact())
                .registrationNo(r.getRegistrationNo()).active(r.getActive())
                .build();
    }
}
