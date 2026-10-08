package com.interacthub.service;

import com.interacthub.dto.auth.*;
import com.interacthub.entity.User;
import com.interacthub.exception.*;
import com.interacthub.repository.UserRepository;
import com.interacthub.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.UUID;

@Service @RequiredArgsConstructor @Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) throw new DuplicateResourceException("Email already in use");
        if (userRepository.existsByUsername(req.username())) throw new DuplicateResourceException("Username already taken");
        User user = User.builder()
            .id(UUID.randomUUID().toString()).username(req.username()).email(req.email())
            .passwordHash(passwordEncoder.encode(req.password()))
            .displayName(req.displayName() != null && !req.displayName().isBlank() ? req.displayName() : req.username())
            .role("ROLE_USER").createdAt(LocalDateTime.now(ZoneOffset.UTC)).build();
        userRepository.save(user);
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), user.getRole());
        log.info("User registered: {}", user.getEmail());
        return new AuthResponse(user.getId(), user.getUsername(), user.getEmail(), user.getDisplayName(), user.getAvatarUrl(), token, user.getRole());
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.email())
            .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash()))
            throw new UnauthorizedException("Invalid email or password");
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), user.getRole());
        return new AuthResponse(user.getId(), user.getUsername(), user.getEmail(), user.getDisplayName(), user.getAvatarUrl(), token, user.getRole());
    }
}
