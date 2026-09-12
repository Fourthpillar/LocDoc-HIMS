package com.lockdoc.app.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Validates the "Authorization: Bearer &lt;token&gt;" header on every request
 * except the ones configured under "app.security.jwt.excluded-urls".
 *
 * Note: matching is done against request.getServletPath(), which is the path
 * relative to the application's context-path (/lockdoc), so exclude patterns
 * should be written WITHOUT the context-path prefix, e.g. "/auth/login".
 *
 * <b>Writes 401 itself for a missing/invalid/expired token, rather than
 * falling through to Spring Security's default.</b> SecurityConfig
 * configures no {@code AuthenticationEntryPoint}, so an unauthenticated
 * request against {@code .anyRequest().authenticated()} would otherwise
 * fall back to Spring's default 403 - which the frontend's api.ts never
 * treats as "please log in again" (only 401 triggers its clear-token/
 * redirect-to-/login handling, per its own header comment). Without this,
 * every token expiry (the normal case after the JWT's exp elapses, not
 * an edge case) leaves the whole app silently 403ing on every request
 * with no way back to the login screen short of a manual reload.
 */
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final List<String> excludedUrls;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService, List<String> excludedUrls) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.excludedUrls = excludedUrls;
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getServletPath();
        return excludedUrls.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendUnauthorized(response, "Missing bearer token");
            return;
        }

        final String token = authHeader.substring(7);

        try {
            final String username = jwtUtil.extractUsername(token);

            if (username == null) {
                sendUnauthorized(response, "Invalid token");
                return;
            }

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (!jwtUtil.isTokenValid(token, userDetails.getUsername())) {
                    sendUnauthorized(response, "Token expired or invalid");
                    return;
                }

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (Exception e) {
            log.warn("JWT authentication failed: {}", e.getMessage());
            sendUnauthorized(response, "Token expired or invalid");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write(
                "{\"error\":\"Unauthorized\",\"message\":\"" + message + "\",\"status\":401}");
    }
}
