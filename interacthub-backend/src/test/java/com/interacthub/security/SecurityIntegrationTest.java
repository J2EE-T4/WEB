package com.interacthub.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.interacthub.dto.auth.RegisterRequest;
import com.interacthub.filter.RateLimiterService;
import com.interacthub.service.AuthService;
import com.interacthub.service.ProfileService;
import com.interacthub.service.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private ProfileService profileService;

    @MockBean
    private ReportService reportService;

    @MockBean
    private RateLimiterService rateLimiterService;

    // Mock Redis infrastructure — no live Redis needed in tests
    @MockBean
    private StringRedisTemplate stringRedisTemplate;

    // Mock WebSocket infrastructure — no live broker needed in tests
    @MockBean
    private SimpMessagingTemplate simpMessagingTemplate;

    @Test
    void accessProtectedResource_WithoutToken_Returns401Json() throws Exception {
        when(rateLimiterService.tryAcquire(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyInt(), org.mockito.ArgumentMatchers.anyLong()))
                .thenReturn(true);

        mockMvc.perform(get("/api/profile/me")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.message", containsString("Full authentication is required")));
    }

    @Test
    @WithMockUser(username = "regular_user", roles = {"USER"})
    void accessAdminResource_WithRoleUser_Returns403Json() throws Exception {
        when(rateLimiterService.tryAcquire(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyInt(), org.mockito.ArgumentMatchers.anyLong()))
                .thenReturn(true);

        mockMvc.perform(get("/api/admin/moderation/reported-posts")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.message", containsString("Access is denied")));
    }

    @Test
    @WithMockUser(username = "admin_user", roles = {"ADMIN"})
    void accessAdminResource_WithRoleAdmin_Allowed() throws Exception {
        when(rateLimiterService.tryAcquire(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyInt(), org.mockito.ArgumentMatchers.anyLong()))
                .thenReturn(true);

        mockMvc.perform(get("/api/admin/moderation/reported-posts")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void registerWithInvalidData_Returns400WithValidationErrors() throws Exception {
        when(rateLimiterService.tryAcquire(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyInt(), org.mockito.ArgumentMatchers.anyLong()))
                .thenReturn(true);

        // Invalid: empty username, bad email, short password
        RegisterRequest invalidReq = new RegisterRequest("", "invalid-email", "123", "");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", is("Validation failed")))
                .andExpect(jsonPath("$.errors.username", notNullValue()))
                .andExpect(jsonPath("$.errors.email", notNullValue()))
                .andExpect(jsonPath("$.errors.password", notNullValue()));
    }

    @Test
    void rateLimiterExceeded_Returns429TooManyRequests() throws Exception {
        when(rateLimiterService.tryAcquire(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyInt(), org.mockito.ArgumentMatchers.anyLong()))
                .thenReturn(false);
        when(rateLimiterService.getRetryAfterSeconds(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(45L);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test@example.com\",\"password\":\"123456\"}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "45"))
                .andExpect(jsonPath("$.status", is(429)))
                .andExpect(jsonPath("$.message", containsString("Too many requests")));
    }
}
