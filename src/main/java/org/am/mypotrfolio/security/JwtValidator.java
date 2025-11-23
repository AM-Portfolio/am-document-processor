package org.am.mypotrfolio.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.am.mypotrfolio.config.JwtConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Validates JWT tokens from API Gateway
 * Uses INTERNAL_JWT_SECRET to validate service tokens
 * 
 * Per coding instructions:
 * - Validates service tokens from API Gateway
 * - Returns user_id for authorization
 * - Throws exception if token is invalid
 */
@Slf4j
@Component
public class JwtValidator {
    
    @Autowired
    private JwtConfig jwtConfig;
    
    /**
     * Validate service-to-service JWT token from API Gateway
     * 
     * Per instructions: "Service tokens generated at API Gateway, not user tokens passed through"
     * This validates the service token that the gateway creates
     * 
     * @param token JWT token from Authorization header
     * @return user_id extracted from token
     * @throws SecurityException if token is invalid
     */
    public String validateServiceToken(String token) {
        try {
            log.debug("Validating service token");
            
            // Parse and validate token using INTERNAL_JWT_SECRET
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(jwtConfig.getInternalSecret().getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody();
            
            // Verify token type is "service"
            String tokenType = (String) claims.get("type");
            if (!"service".equals(tokenType)) {
                log.warn("Token type mismatch. Expected 'service', got: {}", tokenType);
                throw new IllegalArgumentException("Invalid token type: " + tokenType);
            }
            
            // Verify token hasn't expired
            Date expiration = claims.getExpiration();
            if (expiration.before(new Date())) {
                log.warn("Token has expired");
                throw new IllegalArgumentException("Token expired at: " + expiration);
            }
            
            // Extract and return user_id
            String userId = (String) claims.get("user_id");
            String serviceId = (String) claims.get("service_id");
            
            log.debug("Token validated successfully. User: {}, Service: {}", userId, serviceId);
            return userId;
            
        } catch (JwtException e) {
            log.error("JWT validation failed: {}", e.getMessage());
            throw new SecurityException("Invalid JWT token: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            log.error("Token validation error: {}", e.getMessage());
            throw new SecurityException("Token validation failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Extract all claims from token for debugging/logging
     */
    public Claims getTokenClaims(String token) {
        try {
            return Jwts.parserBuilder()
                .setSigningKey(jwtConfig.getInternalSecret().getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody();
        } catch (JwtException e) {
            throw new SecurityException("Invalid token: " + e.getMessage(), e);
        }
    }
}