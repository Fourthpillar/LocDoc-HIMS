package com.lockdoc.app;

import com.lockdoc.app.config.CorsProperties;
import com.lockdoc.app.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({JwtProperties.class, CorsProperties.class})
public class LockDocApplication {

    public static void main(String[] args) {
        SpringApplication.run(LockDocApplication.class, args);
    }
}
