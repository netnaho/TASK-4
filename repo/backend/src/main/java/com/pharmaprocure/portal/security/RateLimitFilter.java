package com.pharmaprocure.portal.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pharmaprocure.portal.dto.ApiErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final int maxRequestsPerMinute;
    private final int maxLoginRequestsPerMinute;
    private final Map<String, Deque<Instant>> requestWindows = new ConcurrentHashMap<>();
    private final Map<String, Deque<Instant>> loginIpWindows = new ConcurrentHashMap<>();
    private final Map<String, Deque<Instant>> authIpWindows = new ConcurrentHashMap<>();
    private final Clock clock;
    private final ObjectMapper objectMapper;

    @Autowired
    public RateLimitFilter(
            ObjectMapper objectMapper,
            @org.springframework.beans.factory.annotation.Value("${rate.limit.max-per-minute:60}") int maxRequestsPerMinute,
            @org.springframework.beans.factory.annotation.Value("${rate.limit.login.max-per-minute:20}") int maxLoginRequestsPerMinute) {
        this(objectMapper, Clock.systemUTC(), maxRequestsPerMinute, maxLoginRequestsPerMinute);
    }

    RateLimitFilter(ObjectMapper objectMapper, Clock clock, int maxRequestsPerMinute, int maxLoginRequestsPerMinute) {
        this.objectMapper = objectMapper;
        this.clock = clock;
        this.maxRequestsPerMinute = maxRequestsPerMinute;
        this.maxLoginRequestsPerMinute = maxLoginRequestsPerMinute;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/health") || path.startsWith("/api/meta") || path.startsWith("/actuator");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();

        if (path.startsWith("/api/auth/login")) {
            String clientIp = resolveClientIp(request);
            if (isRateLimited(loginIpWindows, clientIp, maxLoginRequestsPerMinute)) {
                writeRateLimitResponse(response, "MAX_" + maxLoginRequestsPerMinute + "_LOGIN_REQUESTS_PER_MINUTE");
                return;
            }
            filterChain.doFilter(request, response);
            return;
        }

        if (path.startsWith("/api/auth/")) {
            String clientIp = resolveClientIp(request);
            if (isRateLimited(authIpWindows, clientIp, maxRequestsPerMinute)) {
                writeRateLimitResponse(response, "MAX_" + maxRequestsPerMinute + "_REQUESTS_PER_MINUTE");
                return;
            }
            filterChain.doFilter(request, response);
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = authentication.getName();
        if (isRateLimited(requestWindows, key, maxRequestsPerMinute)) {
            writeRateLimitResponse(response, "MAX_" + maxRequestsPerMinute + "_REQUESTS_PER_MINUTE");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isRateLimited(Map<String, Deque<Instant>> windows, String key, int maxRequests) {
        Deque<Instant> window = windows.computeIfAbsent(key, ignored -> new ArrayDeque<>());
        Instant now = clock.instant();
        synchronized (window) {
            while (!window.isEmpty() && window.peekFirst().isBefore(now.minusSeconds(60))) {
                window.pollFirst();
            }
            if (window.size() >= maxRequests) {
                return true;
            }
            window.addLast(now);
        }
        return false;
    }

    private void writeRateLimitResponse(HttpServletResponse response, String detail) throws IOException {
        response.setStatus(429);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), new ApiErrorResponse(429, "Rate limit exceeded", List.of(detail)));
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    public void clearWindows() {
        requestWindows.clear();
        loginIpWindows.clear();
        authIpWindows.clear();
    }
}
