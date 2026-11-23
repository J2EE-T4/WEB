package com.interacthub.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger UI configuration.
 *
 * <p>Defines the global BearerAuth security scheme so that the Swagger UI
 * shows an "Authorize 🔒" button. Users paste their JWT access token there
 * and all subsequent "Try it out" requests will include the
 * {@code Authorization: Bearer <token>} header automatically.
 *
 * <p>Usage in Swagger UI:
 * <ol>
 *   <li>Call {@code POST /api/auth/login} → copy {@code accessToken}</li>
 *   <li>Click the green <strong>Authorize 🔒</strong> button (top-right)</li>
 *   <li>Paste the token into the <em>BearerAuth (http, Bearer)</em> field</li>
 *   <li>Click <strong>Authorize</strong> → <strong>Close</strong></li>
 *   <li>All protected endpoints now work in "Try it out"</li>
 * </ol>
 */
@Configuration
@OpenAPIDefinition(
    info = @Info(
        title       = "InteractHub API",
        version     = "1.0.0",
        description = "RESTful backend for the InteractHub social network platform",
        contact     = @Contact(name = "InteractHub Team")
    ),
    servers = {
        @Server(url = "http://localhost:8080", description = "Local development server")
    },
    // Apply BearerAuth globally — every endpoint requires a token by default.
    // Public endpoints (login, register, etc.) still work because they are
    // whitelisted in SecurityConfig, but showing the lock icon is harmless.
    security = @SecurityRequirement(name = "BearerAuth")
)
@SecurityScheme(
    name         = "BearerAuth",
    type         = SecuritySchemeType.HTTP,
    scheme       = "bearer",
    bearerFormat = "JWT",
    in           = SecuritySchemeIn.HEADER,
    description  = "Paste your JWT access token here (without the 'Bearer ' prefix)"
)
public class OpenApiConfig {
    // No beans needed — annotations do all the work via springdoc auto-configuration
}
