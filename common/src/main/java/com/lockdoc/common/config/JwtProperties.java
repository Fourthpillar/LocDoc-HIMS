package com.lockdoc.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Binds to the "app.security.jwt.*" properties in application.yml.
 * "excludedUrls" holds the Ant-style patterns that bypass JWT validation
 * (e.g. /auth/login, /actuator/health, /h2-console/**).
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.security.jwt")
public class JwtProperties {

    private String secret;
    private long expirationMs;
    private String issuer;
    private List<String> excludedUrls = new ArrayList<>();
}
