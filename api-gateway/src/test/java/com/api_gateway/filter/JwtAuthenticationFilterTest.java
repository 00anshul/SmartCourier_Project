package com.api_gateway.filter;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @Mock
    private FilterChain filterChain;

    // Same secret as in application.properties
    private static final String SECRET =
            "5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(filter, "secret", SECRET);
    }

    // ── Helper to generate a valid token ──────────────────────────────────
    private String generateValidToken() {
        SecretKey key = Keys.hmacShaKeyFor(hexToBytes(SECRET));
        return Jwts.builder()
                .subject("test@example.com")
                .claim("userId", 1L)
                .claim("role", "CUSTOMER")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 900_000))
                .signWith(key)
                .compact();
    }

    private byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    // ── Test 1: Public path is allowed through without token ──────────────
    @Test
    void publicPath_shouldSkipJwtCheck() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST",
                "/gateway/auth/login");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        // Filter chain should proceed — no 401
        verify(filterChain, times(1)).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    // ── Test 2: Missing Authorization header returns 401 ──────────────────
    @Test
    void missingAuthHeader_shouldReturn401() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET",
                "/gateway/deliveries/my");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString())
                .contains("Missing or invalid Authorization header");
        verify(filterChain, never()).doFilter(any(), any());
    }

    // ── Test 3: Malformed header (no Bearer prefix) returns 401 ───────────
    @Test
    void malformedAuthHeader_shouldReturn401() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET",
                "/gateway/deliveries/my");
        request.addHeader("Authorization", "Token abc123");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(401);
        verify(filterChain, never()).doFilter(any(), any());
    }

    // ── Test 4: Invalid/tampered token returns 401 ────────────────────────
    @Test
    void invalidToken_shouldReturn401() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET",
                "/gateway/deliveries/my");
        request.addHeader("Authorization", "Bearer this.is.not.a.valid.token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString())
                .contains("Invalid or expired token");
        verify(filterChain, never()).doFilter(any(), any());
    }

    // ── Test 5: Valid token passes through and sets attributes ────────────
    @Test
    void validToken_shouldPassThroughAndSetAttributes() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET",
                "/gateway/deliveries/my");
        request.addHeader("Authorization", "Bearer " + generateValidToken());
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertThat(request.getAttribute("userId")).isNotNull();
        assertThat(request.getAttribute("role")).isEqualTo("CUSTOMER");
    }

    // ── Test 6: Swagger path is public ────────────────────────────────────
    @Test
    void swaggerPath_shouldSkipJwtCheck() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET",
                "/swagger-ui/index.html");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(200);
    }
}