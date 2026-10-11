package com.interacthub.service;

import com.interacthub.dto.auth.LoginRequest;
import com.interacthub.dto.auth.RegisterRequest;
import com.interacthub.entity.User;
import com.interacthub.exception.DuplicateResourceException;
import com.interacthub.exception.UnauthorizedException;
import com.interacthub.repository.UserRepository;
import com.interacthub.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

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

    @InjectMocks
    private AuthService authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id("u-123")
                .username("john_doe")
                .email("john@example.com")
                .passwordHash("hashed_pwd")
                .role("ROLE_USER")
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

        var response = authService.register(req);

        assertNotNull(response);
        assertEquals("john_doe", response.username());
        assertEquals("mocked_jwt", response.token());
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

        var response = authService.login(req);

        assertNotNull(response);
        assertEquals("valid_jwt", response.token());
    }

    @Test
    void login_InvalidPassword_ThrowsException() {
        LoginRequest req = new LoginRequest("john@example.com", "wrong_password");
        when(userRepository.findByEmail(req.email())).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches(req.password(), sampleUser.getPasswordHash())).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authService.login(req));
    }
}
