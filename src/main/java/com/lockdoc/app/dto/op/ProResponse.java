package com.lockdoc.app.dto.op;

import com.lockdoc.app.entity.op.Pro;
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
