package com.lockdoc.outpatient.dto;

import com.lockdoc.outpatient.entity.Pro;
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
public class ProResponse {

    private Long id;
    private String name;
    private String contact;
    private Boolean active;

    public static ProResponse toResponse(Pro p) {
        return ProResponse.builder().id(p.getId()).name(p.getName()).contact(p.getContact()).active(p.getActive()).build();
    }
}
