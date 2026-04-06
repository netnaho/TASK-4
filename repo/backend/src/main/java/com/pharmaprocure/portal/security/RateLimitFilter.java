package com.pharmaprocure.portal.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pharmaprocure.portal.dto.ApiErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetAddress;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Arrays;
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
    private final int maxInfraRequestsPerMinute;
    private final List<String> trustedProxyCidrs;
    private final Map<String, Deque<Instant>> requestWindows = new ConcurrentHashMap<>();
    private final Map<String, Deque<Instant>> loginIpWindows = new ConcurrentHashMap<>();
    private final Map<String, Deque<Instant>> infraIpWindows = new ConcurrentHashMap<>();
    private final Clock clock;
    private final ObjectMapper objectMapper;

    @Autowired
    public RateLimitFilter(
            ObjectMapper objectMapper,
            @org.springframework.beans.factory.annotation.Value("${rate.limit.max-per-minute:60}") int maxRequestsPerMinute,
            @org.springframework.beans.factory.annotation.Value("${rate.limit.login.max-per-minute:20}") int maxLoginRequestsPerMinute,
            @org.springframework.beans.factory.annotation.Value("${rate.limit.infra.max-per-minute:120}") int maxInfraRequestsPerMinute,
            @org.springframework.beans.factory.annotation.Value("${rate.limit.trusted-proxies:}") String trustedProxiesConfig) {
        this(objectMapper, Clock.systemUTC(), maxRequestsPerMinute, maxLoginRequestsPerMinute,
             maxInfraRequestsPerMinute, parseTrustedProxies(trustedProxiesConfig));
    }

    RateLimitFilter(ObjectMapper objectMapper, Clock clock, int maxRequestsPerMinute,
                    int maxLoginRequestsPerMinute, int maxInfraRequestsPerMinute, List<String> trustedProxyCidrs) {
        this.objectMapper = objectMapper;
        this.clock = clock;
        this.maxRequestsPerMinute = maxRequestsPerMinute;
        this.maxLoginRequestsPerMinute = maxLoginRequestsPerMinute;
        this.maxInfraRequestsPerMinute = maxInfraRequestsPerMinute;
        this.trustedProxyCidrs = trustedProxyCidrs;
    }

    private static List<String> parseTrustedProxies(String config) {
        if (config == null || config.isBlank()) return List.of();
        return Arrays.stream(config.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/health") || path.startsWith("/api/meta") || path.startsWith("/actuator");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        String clientIp = resolveClientIp(request);

        if (path.startsWith("/api/auth/csrf") || path.startsWith("/api/auth/captcha")) {
            if (isRateLimited(infraIpWindows, clientIp, maxInfraRequestsPerMinute)) {
                writeRateLimitResponse(response, "MAX_" + maxInfraRequestsPerMinute + "_INFRA_REQUESTS_PER_MINUTE");
                return;
            }
            filterChain.doFilter(request, response);
            return;
        }

        if (path.startsWith("/api/auth/login")) {
            if (isRateLimited(loginIpWindows, clientIp, maxLoginRequestsPerMinute)) {
                writeRateLimitResponse(response, "MAX_" + maxLoginRequestsPerMinute + "_LOGIN_REQUESTS_PER_MINUTE");
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

    /**
     * Resolves the client IP for rate limiting.
     *
     * X-Forwarded-For is only trusted when the direct TCP peer (remoteAddr) is in
     * the configured trusted-proxy CIDR list.  Without that configuration, an
     * attacker could spoof any IP by adding the header themselves, bypassing the
     * per-IP login rate limit.
     */
    private String resolveClientIp(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        if (isTrustedProxy(remoteAddr)) {
            String forwarded = request.getHeader("X-Forwarded-For");
            if (forwarded != null && !forwarded.isBlank()) {
                return forwarded.split(",")[0].trim();
            }
        }
        return remoteAddr;
    }

    private boolean isTrustedProxy(String remoteAddr) {
        if (trustedProxyCidrs.isEmpty()) {
            return false;
        }
        try {
            InetAddress remote = InetAddress.getByName(remoteAddr);
            for (String cidr : trustedProxyCidrs) {
                if (matchesCidr(remote, cidr)) {
                    return true;
                }
            }
        } catch (Exception ignored) {}
        return false;
    }

    private boolean matchesCidr(InetAddress address, String cidr) {
        try {
            String[] parts = cidr.split("/");
            InetAddress network = InetAddress.getByName(parts[0]);
            int prefix = parts.length > 1 ? Integer.parseInt(parts[1]) : 32;
            byte[] addrBytes = address.getAddress();
            byte[] netBytes = network.getAddress();
            if (addrBytes.length != netBytes.length) return false;
            int bitsChecked = 0;
            for (int i = 0; i < addrBytes.length && bitsChecked < prefix; i++) {
                int bits = Math.min(8, prefix - bitsChecked);
                int mask = 0xFF & (0xFF << (8 - bits));
                if ((addrBytes[i] & mask) != (netBytes[i] & mask)) return false;
                bitsChecked += bits;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void clearWindows() {
        requestWindows.clear();
        loginIpWindows.clear();
        infraIpWindows.clear();
    }
}
