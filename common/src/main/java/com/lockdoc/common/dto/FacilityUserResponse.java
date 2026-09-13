package com.lockdoc.common.dto;

import com.lockdoc.common.entity.User;
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
public class FacilityUserResponse {

    private Long id;
    private String username;
    private String fullName;
    private String email;
    private String roleCode;
    private Boolean enabled;
    private Boolean mustChangePassword;
    private LocalDateTime createdDate;

    public static FacilityUserResponse toResponse(User u) {
        return FacilityUserResponse.builder()
                .id(u.getId())
                .username(u.getUsername())
                .fullName(u.getFullName())
                .email(u.getEmail())
                .roleCode(u.getRoles().stream().findFirst().map(r -> r.getRoleCode()).orElse(null))
                .enabled(u.getEnabled())
                .mustChangePassword(u.getMustChangePassword())
                .createdDate(u.getCreatedDate())
                .build();
    }
}
