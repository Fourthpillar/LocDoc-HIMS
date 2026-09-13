package com.lockdoc.app;

import com.lockdoc.common.config.CorsProperties;
import com.lockdoc.common.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Components, entities and repositories live in the {@code common}, {@code outpatient}
 * and {@code doctor} modules under {@code com.lockdoc.*}, not beneath this class's own
 * package, so all three scans are widened to {@code com.lockdoc}.
 */
@SpringBootApplication(scanBasePackages = "com.lockdoc")
@EntityScan("com.lockdoc")
@EnableJpaRepositories("com.lockdoc")
@EnableConfigurationProperties({JwtProperties.class, CorsProperties.class})
public class LockDocApplication {

    public static void main(String[] args) {
        SpringApplication.run(LockDocApplication.class, args);
    }
}
