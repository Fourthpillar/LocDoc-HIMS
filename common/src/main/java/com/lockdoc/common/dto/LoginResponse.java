package com.lockdoc.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    private String token;
    private String username;
    private List<String> roles;
    private List<String> rights;
    /** The facility this user acts in — null for Super Admin and for doctors. */
    private Long facilityId;
    private String facilityName;

    @Builder.Default
    private String tokenType = "Bearer";
}
