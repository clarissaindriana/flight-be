package apap.ti._5.flight_2306211660_be.restcontroller;

import apap.ti._5.flight_2306211660_be.config.security.ProfileClient;
import apap.ti._5.flight_2306211660_be.restcontroller.auth.AuthProxyController;
import apap.ti._5.flight_2306211660_be.restdto.response.BaseResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthRestControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProfileClient profileClient;

    @InjectMocks
    private AuthProxyController controller;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("POST /api/auth/login: success sets cookie and returns 200")
    void login_success() throws Exception {
        var loginResponse = new ProfileClient.LoginResponse();
        loginResponse.setToken("jwt-token");
        when(profileClient.login(any(ProfileClient.LoginRequest.class))).thenReturn(loginResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@example.com\",\"password\":\"pass\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("User logged in successfully"))
                .andExpect(jsonPath("$.data.token").value("jwt-token"))
                .andExpect(header().string("Set-Cookie", "JWT_TOKEN=jwt-token; Path=/; Secure; HttpOnly; SameSite=None"));
    }

    @Test
    @DisplayName("POST /api/auth/login: missing email/password returns 401")
    void login_missingCredentials() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"\",\"password\":\"\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Invalid credentials or profile service error"));
    }

    @Test
    @DisplayName("POST /api/auth/login: profile service returns null -> 401")
    void login_invalidCredentials() throws Exception {
        when(profileClient.login(any(ProfileClient.LoginRequest.class))).thenReturn(null);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@example.com\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Invalid credentials or profile service error"));
    }

    @Test
    @DisplayName("POST /api/auth/login: exception -> 500")
    void login_exception() throws Exception {
        when(profileClient.login(any(ProfileClient.LoginRequest.class))).thenThrow(new RuntimeException("profile error"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@example.com\",\"password\":\"pass\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Error: profile error"));
    }

    @Test
    @DisplayName("POST /api/auth/register: success proxies to profile service")
    void register_success() throws Exception {
        when(profileClient.register(any())).thenReturn(Map.of("userId", "123"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"new@example.com\",\"password\":\"pass\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Register proxied"))
                .andExpect(jsonPath("$.data.userId").value("123"));
    }

    @Test
    @DisplayName("POST /api/auth/register: exception -> 500")
    void register_exception() throws Exception {
        when(profileClient.register(any())).thenThrow(new RuntimeException("register error"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"new@example.com\",\"password\":\"pass\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Error: register error"));
    }

    @Test
    @DisplayName("POST /api/auth/logout: clears cookie and returns 200")
    void logout_success() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Logged out (cookie cleared)"))
                .andExpect(header().string("Set-Cookie", "JWT_TOKEN=; Path=/; Max-Age=0; Expires=Thu, 01 Jan 1970 00:00:00 GMT; Secure; HttpOnly; SameSite=None"));
    }
}
