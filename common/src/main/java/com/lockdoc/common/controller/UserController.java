package com.lockdoc.common.controller;

import com.lockdoc.common.entity.User;
import com.lockdoc.common.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public Map<String, Object> me(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow();

        return Map.of(
                "username", user.getUsername(),
                "email", user.getEmail() == null ? "" : user.getEmail(),
                "fullName", user.getFullName() == null ? "" : user.getFullName(),
                "roles", user.getRoles().stream().map(r -> r.getRoleCode()).toList()
        );
    }
}
