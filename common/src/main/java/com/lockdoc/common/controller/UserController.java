package com.lockdoc.common.controller;

import com.lockdoc.common.config.AppUserPrincipal;
import com.lockdoc.common.entity.User;
import com.lockdoc.common.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * This controller is NOT in the excluded-urls list, so every request
 * must carry a valid "Authorization: Bearer <token>" header.
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/me")
    public Map<String, Object> me(@AuthenticationPrincipal AppUserPrincipal principal) {
        User user = userRepository.findById(principal.getUserId())
                .orElseThrow();

        Map<String, Object> response = new HashMap<>();
        response.put("username", user.getUsername());
        response.put("email", user.getEmail() == null ? "" : user.getEmail());
        response.put("fullName", user.getFullName() == null ? "" : user.getFullName());
        response.put("roles", user.getRoles().stream().map(r -> r.getRoleCode()).toList());
        // null only for Super Admin - see AppUserPrincipal.
        response.put("facilityId", principal.getFacilityId());
        return response;
    }
}
