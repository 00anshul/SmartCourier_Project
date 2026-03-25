package com.api_gateway.filter;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${jwt.secret}")
    private String secret;

    private static final List<String> PUBLIC_PATHS = List.of(
            "/gateway/auth/register",
            "/gateway/auth/login",
            "/gateway/auth/refresh",
            "/actuator",
            "/v3/api-docs",
            "/swagger-ui",
            "/gateway/auth/v3/api-docs",
            "/gateway/deliveries/v3/api-docs",
            "/gateway/tracking/v3/api-docs",
            "/gateway/admin/v3/api-docs",
            "/gateway/auth-docs",
            "/gateway/delivery-docs",
            "/gateway/tracking-docs",
            "/gateway/admin-docs"
    );

    private SecretKey getSigningKey() {
        byte[] keyBytes = hexStringToByteArray(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private byte[] hexStringToByteArray(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // Skip JWT check for public paths
        boolean isPublic = PUBLIC_PATHS.stream()
                .anyMatch(path::startsWith);

        if (isPublic) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("Missing or invalid Authorization header");
            return;
        }

        String token = authHeader.substring(7);

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // Forward user info to downstream services as headers
            request.setAttribute("userId", claims.get("userId"));
            request.setAttribute("role",   claims.get("role"));

            filterChain.doFilter(request, response);

        } catch (JwtException e) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("Invalid or expired token");
        }
    }
}