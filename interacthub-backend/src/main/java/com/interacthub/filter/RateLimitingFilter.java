package com.interacthub.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.interacthub.exception.GlobalExceptionHandler.ErrorResponse;
import com.interacthub.security.CustomUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class RateLimitingFilter extends OncePerRequestFilter {

    private final RateLimiterService rateLimiterService;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Value("${app.rate-limit.enabled:true}")
    private boolean enabled;

    @Value("${app.rate-limit.capacity:100}")
    private int capacity;

    @Value("${app.rate-limit.window-seconds:60}")
    private long windowSeconds;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (!enabled || isExcludedPath(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientKey = resolveClientKey(request);

        if (!rateLimiterService.tryAcquire(clientKey, capacity, windowSeconds)) {
            long retryAfter = rateLimiterService.getRetryAfterSeconds(clientKey);
            log.warn("Rate limit exceeded for client: {} on URI: {}", clientKey, request.getRequestURI());

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json;charset=UTF-8");
            response.setHeader("Retry-After", String.valueOf(retryAfter));

            ErrorResponse error = new ErrorResponse(
                    HttpStatus.TOO_MANY_REQUESTS.value(),
                    "Too many requests. Please try again after " + retryAfter + " seconds.",
                    null,
                    LocalDateTime.now()
            );

            response.getWriter().write(objectMapper.writeValueAsString(error));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String resolveClientKey(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            return "user:" + userDetails.getId();
        }

        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return "ip:" + xForwardedFor.split(",")[0].trim();
        }

        return "ip:" + request.getRemoteAddr();
    }

    private boolean isExcludedPath(String uri) {
        return uri.startsWith("/swagger-ui") ||
               uri.startsWith("/v3/api-docs") ||
               uri.equals("/swagger-ui.html") ||
               uri.startsWith("/ws");
    }
}
