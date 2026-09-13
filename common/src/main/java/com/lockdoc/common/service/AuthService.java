package com.lockdoc.common.service;

import com.lockdoc.common.config.JwtUtil;
import com.lockdoc.common.dto.LoginRequest;
import com.lockdoc.common.dto.LoginResponse;
import com.lockdoc.common.entity.Right;
import com.lockdoc.common.entity.Role;
import com.lockdoc.common.entity.User;
import com.lockdoc.common.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("User not found after authentication"));

        List<String> roleCodes = user.getRoles().stream()
                .map(Role::getRoleCode)
                .collect(Collectors.toList());

        List<String> rightCodes = user.getRoles().stream()
                .flatMap(role -> role.getRights().stream())
                .map(Right::getRightCode)
                .distinct()
                .collect(Collectors.toList());

        Long facilityId = user.getFacility() != null ? user.getFacility().getId() : null;
        String token = jwtUtil.generateToken(user.getUsername(), roleCodes, rightCodes, facilityId);

        return LoginResponse.builder()
                .token(token)
                .username(user.getUsername())
                .roles(roleCodes)
                .rights(rightCodes)
                .facilityId(facilityId)
                .build();
    }
}
