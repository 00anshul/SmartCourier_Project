package com.auth_service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.auth_service.controller.AuthController;
import com.auth_service.dto.AuthResponse;
import com.auth_service.dto.LoginRequest;
import com.auth_service.dto.RegisterRequest;
import com.auth_service.entity.User;
import com.auth_service.service.AuthService;
import com.auth_service.security.JwtUtil; // Check this import matches your project
// import com.auth_service.security.CustomUserDetailsService; // Uncomment and update if needed
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false) // Bypasses security filters for tests
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    // ── THE FIX: MOCK MISSING SECURITY BEANS ─────────────────────────
    @MockitoBean
    private JwtUtil jwtUtil; 

    // If your filter also injects a UserDetailsService, you will need to mock it too.
    // Uncomment the line below if the test crashes complaining about a UserDetailsService next:
    // @MockitoBean
    // private CustomUserDetailsService customUserDetailsService; 
    // ───────────────────────────────────────────────────────────────

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private AuthResponse authResponse;
    private User testUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setFullName("John Doe");
        registerRequest.setEmail("john@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setPhone("9999999999");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("john@example.com");
        loginRequest.setPassword("password123");

        authResponse = new AuthResponse(
                "mock.jwt.token", "CUSTOMER", 1L, "John Doe");

        testUser = new User();
        testUser.setId(1L);
        testUser.setFullName("John Doe");
        testUser.setEmail("john@example.com");
        testUser.setRole("CUSTOMER");
        testUser.setPhone("9999999999");
    }

    // ── Register ──────────────────────────────────────────────────────────

    @Test
    void register_success_shouldReturn201() throws Exception {
        when(authService.register(any(RegisterRequest.class))).thenReturn(testUser);

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Registration successful"))
                .andExpect(jsonPath("$.data").isEmpty());

        verify(authService).register(any(RegisterRequest.class));
    }

    @Test
    void register_missingFields_shouldReturn400() throws Exception {
        RegisterRequest invalid = new RegisterRequest();
        invalid.setEmail("john@example.com");

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    // ── Login ─────────────────────────────────────────────────────────────

    @Test
    void login_success_shouldReturn200WithToken() throws Exception {
        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.data.accessToken").value("mock.jwt.token"))
                .andExpect(jsonPath("$.data.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.data.userId").value(1));

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    void login_missingFields_shouldReturn400() throws Exception {
        LoginRequest invalid = new LoginRequest();
        invalid.setEmail("john@example.com");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    // ── Logout ────────────────────────────────────────────────────────────

    @Test
    void logout_success_shouldReturn200() throws Exception {
        doNothing().when(authService).logout(1L);

        mockMvc.perform(post("/auth/logout")
                .header("X-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Logged out successfully"))
                .andExpect(jsonPath("$.data").isEmpty());

        verify(authService).logout(1L);
    }

    @Test
    void logout_missingHeader_shouldReturn400() throws Exception {
        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isBadRequest());
    }

    // ── GetUserById ───────────────────────────────────────────────────────

    @Test
    void getUserById_found_shouldReturn200() throws Exception {
        when(authService.getUserById(1L)).thenReturn(testUser);

        mockMvc.perform(get("/auth/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User found"))
                .andExpect(jsonPath("$.data.email").value("john@example.com"))
                .andExpect(jsonPath("$.data.id").value(1));

        verify(authService).getUserById(1L);
    }
}