package org.am.mypotrfolio.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import lombok.Data;

/**
 * Reads JWT configuration from application.yml
 * Maps to auth.jwt.* properties
 */
@Data
@Component
@Configuration
@ConfigurationProperties(prefix = "auth.jwt")
public class JwtConfig {
    
    // User JWT settings (from JWT_SECRET env var)
    private String secret;
    
    // Service JWT settings (from INTERNAL_JWT_SECRET env var)
    private String internalSecret;
    
    // Token expiration times (in seconds)
    private long expiration;
    private long refreshExpiration;
    private long serviceExpiration;
    
    // Algorithm
    private String algorithm;
}