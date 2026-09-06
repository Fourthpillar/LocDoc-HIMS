package com.lockdoc.app.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Binds to "app.cors.allowed-origins" - the list of UI origins allowed
 * to call this API (e.g. http://localhost:5174/5175/5176).
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.cors")
public class CorsProperties {

    private List<String> allowedOrigins = new ArrayList<>();
}
