package com.interacthub.service;

import com.interacthub.dto.auth.LoginRequest;
import com.interacthub.dto.auth.RefreshTokenRequest;
import com.interacthub.dto.auth.RegisterRequest;
import com.interacthub.dto.auth.TokenRefreshResponse;
import com.interacthub.entity.RefreshToken;
import com.interacthub.entity.User;
import com.interacthub.exception.DuplicateResourceException;
import com.interacthub.exception.UnauthorizedException;
import com.interacthub.repository.UserRepository;
import com.interacthub.security.JwtTokenProvider;
import com.interacthub.security.TokenBlacklistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;
    private RefreshToken sampleRefreshToken;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id("u-123")
                .username("john_doe")
                .email("john@example.com")
                .passwordHash("hashed_pwd")
                .role("ROLE_USER")
                .build();

        sampleRefreshToken = RefreshToken.builder()
                .id("rt-123")
                .token("mock_refresh_token_123")
                .user(sampleUser)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .build();
    }

    @Test
    void register_Success() {
        RegisterRequest req = new RegisterRequest("john_doe", "john@example.com", "secret123", "John Doe");
        when(userRepository.existsByEmail(req.email())).thenReturn(false);
        when(userRepository.existsByUsername(req.username())).thenReturn(false);
        when(passwordEncoder.encode(req.password())).thenReturn("hashed_pwd");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);
        when(jwtTokenProvider.generateToken(any(), any(), any())).thenReturn("mocked_jwt");
        when(refreshTokenService.createRefreshToken(any())).thenReturn(sampleRefreshToken);

        var response = authService.register(req);

        assertNotNull(response);
        assertEquals("john_doe", response.username());
        assertEquals("mocked_jwt", response.token());
        assertEquals("mock_refresh_token_123", response.refreshToken());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_DuplicateEmail_ThrowsException() {
        RegisterRequest req = new RegisterRequest("john_doe", "john@example.com", "secret123", "John Doe");
        when(userRepository.existsByEmail(req.email())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.register(req));
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_Success() {
        LoginRequest req = new LoginRequest("john@example.com", "secret123");
        when(userRepository.findByEmail(req.email())).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches(req.password(), sampleUser.getPasswordHash())).thenReturn(true);
        when(jwtTokenProvider.generateToken(sampleUser.getId(), sampleUser.getUsername(), sampleUser.getRole())).thenReturn("valid_jwt");
        when(refreshTokenService.createRefreshToken(sampleUser.getId())).thenReturn(sampleRefreshToken);

        var response = authService.login(req);

        assertNotNull(response);
        assertEquals("valid_jwt", response.token());
        assertEquals("mock_refresh_token_123", response.refreshToken());
    }

    @Test
    void login_InvalidPassword_ThrowsException() {
        LoginRequest req = new LoginRequest("john@example.com", "wrong_password");
        when(userRepository.findByEmail(req.email())).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches(req.password(), sampleUser.getPasswordHash())).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authService.login(req));
    }

    @Test
    void refreshToken_Success() {
        RefreshTokenRequest req = new RefreshTokenRequest("mock_refresh_token_123");
        when(refreshTokenService.findByToken("mock_refresh_token_123")).thenReturn(Optional.of(sampleRefreshToken));
        when(refreshTokenService.verifyExpiration(sampleRefreshToken)).thenReturn(sampleRefreshToken);
        when(jwtTokenProvider.generateToken(sampleUser.getId(), sampleUser.getUsername(), sampleUser.getRole())).thenReturn("new_access_jwt");
        when(refreshTokenService.createRefreshToken(sampleUser.getId())).thenReturn(sampleRefreshToken);

        TokenRefreshResponse response = authService.refreshToken(req);

        assertNotNull(response);
        assertEquals("new_access_jwt", response.accessToken());
        assertEquals("mock_refresh_token_123", response.refreshToken());
    }

    @Test
    void logout_Success() {
        String token = "Bearer sample_token_string";
        when(jwtTokenProvider.getRemainingTimeMs("sample_token_string")).thenReturn(3600000L);

        authService.logout(sampleUser.getId(), token);

        verify(tokenBlacklistService).blacklistToken(eq("sample_token_string"), anyLong());
        verify(refreshTokenService).revokeAllUserTokens(sampleUser.getId());
    }
}
