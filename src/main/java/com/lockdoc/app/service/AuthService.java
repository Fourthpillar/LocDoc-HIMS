package com.lockdoc.app.service;

import com.lockdoc.app.config.JwtUtil;
import com.lockdoc.app.dto.LoginRequest;
import com.lockdoc.app.dto.LoginResponse;
import com.lockdoc.app.entity.Right;
import com.lockdoc.app.entity.Role;
import com.lockdoc.app.entity.User;
import com.lockdoc.app.repository.UserRepository;
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

        String token = jwtUtil.generateToken(user.getUsername(), roleCodes, rightCodes);

        return LoginResponse.builder()
                .token(token)
                .username(user.getUsername())
                .roles(roleCodes)
                .rights(rightCodes)
                .build();
    }
}
