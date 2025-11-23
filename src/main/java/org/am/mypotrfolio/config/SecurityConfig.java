package org.am.mypotrfolio.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security Configuration for Document Processor
 * 
 * Per coding instructions:
 * - Public endpoints (like /types) don't require authentication
 * - Protected endpoints require service JWT validation (done in controller)
 * - Actuator endpoints should be accessible for health checks
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for stateless REST API
            .csrf(csrf -> csrf.disable())
            
            // Stateless session (JWT-based, no sessions)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Configure authorization
            .authorizeHttpRequests(auth -> auth
                // Public endpoints - no authentication required
                .requestMatchers(
                    "/api/v1/documents/types",      // Document types (public)
                    "/actuator/**",                  // Health checks
                    "/swagger-ui/**",                // Swagger UI
                    "/v3/api-docs/**"                // OpenAPI docs
                ).permitAll()
                
                // All other endpoints - authentication handled in controller
                // (Controller manually validates service JWT using JwtValidator)
                .anyRequest().permitAll()
            )
            
            // Disable HTTP Basic authentication
            .httpBasic(basic -> basic.disable())
            
            // Disable form login
            .formLogin(form -> form.disable());
        
        return http.build();
    }
}
