package com.interacthub.config;

import com.interacthub.entity.User;
import com.interacthub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.time.*;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    @Value("${app.seed.admin-email}") private String adminEmail;
    @Value("${app.seed.admin-password}") private String adminPassword;

    @Override
    public void run(String... args) {
        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            User admin = User.builder()
                .id(UUID.randomUUID().toString()).username("admin").email(adminEmail)
                .passwordHash(passwordEncoder.encode(adminPassword))
                .displayName("Administrator").role("ROLE_ADMIN")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC)).build();
            userRepository.save(admin);
            log.info("Admin user created: {}", adminEmail);
        }
    }
}
