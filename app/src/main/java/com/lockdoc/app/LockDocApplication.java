package com.lockdoc.app;

import com.lockdoc.common.config.CorsProperties;
import com.lockdoc.common.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Sources now span the common/pharmacy/outpatient/app Gradle modules under the
 * shared com.lockdoc root package, so scanning must be explicit rather than
 * relying on @SpringBootApplication's default same-package-and-below scan.
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.lockdoc")
@EntityScan(basePackages = "com.lockdoc")
@EnableJpaRepositories(basePackages = "com.lockdoc")
@EnableConfigurationProperties({JwtProperties.class, CorsProperties.class})
public class LockDocApplication {

    public static void main(String[] args) {
        SpringApplication.run(LockDocApplication.class, args);
    }
}
