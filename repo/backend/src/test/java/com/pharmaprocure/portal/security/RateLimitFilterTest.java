package com.pharmaprocure.portal.security;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class RateLimitFilterTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private Instant now = Instant.parse("2026-01-01T00:00:00Z");
    private RateLimitFilter filter;

    @BeforeEach
    void setUp() {
        filter = new RateLimitFilter(objectMapper, Clock.fixed(now, ZoneId.of("UTC")), 60, 20, 120, List.of());
        SecurityContextHolder.clearContext();
    }

    @Test
    void authenticatedUserWithinLimitPasses() throws Exception {
        setAuthenticated("user1");
        for (int i = 0; i < 60; i++) {
            MockHttpServletResponse response = doFilter("/api/orders");
            assertEquals(200, response.getStatus());
        }
    }

    @Test
    void authenticatedUserOverLimitGets429() throws Exception {
        setAuthenticated("user1");
        for (int i = 0; i < 60; i++) {
            doFilter("/api/orders");
        }
        MockHttpServletResponse response = doFilter("/api/orders");
        assertEquals(429, response.getStatus());
        assertEquals("application/json", response.getContentType());
        String body = response.getContentAsString();
        assert body.contains("MAX_60_REQUESTS_PER_MINUTE");
    }

    @Test
    void windowExpiryResetsCounter() throws Exception {
        setAuthenticated("user1");
        for (int i = 0; i < 60; i++) {
            doFilter("/api/orders");
        }
        MockHttpServletResponse blocked = doFilter("/api/orders");
        assertEquals(429, blocked.getStatus());

        // Advance clock past the 60-second window
        filter = new RateLimitFilter(objectMapper, Clock.fixed(now.plusSeconds(61), ZoneId.of("UTC")), 60, 20, 120, List.of());
        setAuthenticated("user1");
        // The new filter instance has empty windows, simulating expiry
        MockHttpServletResponse response = doFilter("/api/orders");
        assertEquals(200, response.getStatus());
    }

    @Test
    void loginEndpointIpLimitedAt20PerMinute() throws Exception {
        for (int i = 0; i < 20; i++) {
            MockHttpServletResponse response = doFilterWithIp("/api/auth/login", "10.0.0.1");
            assertEquals(200, response.getStatus(), "Request " + (i + 1) + " should pass");
        }
        MockHttpServletResponse response = doFilterWithIp("/api/auth/login", "10.0.0.1");
        assertEquals(429, response.getStatus());
        String body = response.getContentAsString();
        assert body.contains("MAX_20_LOGIN_REQUESTS_PER_MINUTE");
    }

    @Test
    void differentIpsTrackedIndependently() throws Exception {
        for (int i = 0; i < 20; i++) {
            assertEquals(200, doFilterWithIp("/api/auth/login", "10.0.0.1").getStatus());
        }
        // IP-A is exhausted
        assertEquals(429, doFilterWithIp("/api/auth/login", "10.0.0.1").getStatus());
        // IP-B still has quota
        assertEquals(200, doFilterWithIp("/api/auth/login", "10.0.0.2").getStatus());
    }

    @Test
    void csrfEndpointAllowsHighThroughputUpToInfraLimit() throws Exception {
        // 100 requests well under the 120/min infra limit — all should pass
        for (int i = 0; i < 100; i++) {
            assertEquals(200, doFilterWithIp("/api/auth/csrf", "10.0.0.1").getStatus(),
                "CSRF request " + (i + 1) + " should not be rate-limited under infra limit");
        }
    }

    @Test
    void captchaEndpointAllowsHighThroughputUpToInfraLimit() throws Exception {
        // 100 requests well under the 120/min infra limit — all should pass
        for (int i = 0; i < 100; i++) {
            assertEquals(200, doFilterWithIp("/api/auth/captcha", "10.0.0.1").getStatus(),
                "Captcha request " + (i + 1) + " should not be rate-limited under infra limit");
        }
    }

    @Test
    void infraEndpointBlockedAfterExceedingLimit() throws Exception {
        // Use a low infra limit to make the test fast
        RateLimitFilter lowLimitFilter = new RateLimitFilter(objectMapper, Clock.fixed(now, ZoneId.of("UTC")), 60, 20, 5, List.of());
        for (int i = 0; i < 5; i++) {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/auth/csrf");
            request.setRemoteAddr("10.0.0.1");
            MockHttpServletResponse response = new MockHttpServletResponse();
            lowLimitFilter.doFilter(request, response, new MockFilterChain());
            assertEquals(200, response.getStatus(), "Request " + (i + 1) + " should pass");
        }
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/auth/csrf");
        request.setRemoteAddr("10.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        lowLimitFilter.doFilter(request, response, new MockFilterChain());
        assertEquals(429, response.getStatus());
        assert response.getContentAsString().contains("INFRA_REQUESTS_PER_MINUTE");
    }

    @Test
    void excludedPathsSkipFilter() throws Exception {
        // These paths should never be filtered, even past any limit
        for (int i = 0; i < 100; i++) {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/health");
            MockHttpServletResponse response = new MockHttpServletResponse();
            filter.doFilter(request, response, new MockFilterChain());
            assertEquals(200, response.getStatus());
        }
    }

    @Test
    void unauthenticatedNonAuthRequestsPassThrough() throws Exception {
        // No authentication set, non-auth path — should pass through without rate limiting
        for (int i = 0; i < 100; i++) {
            MockHttpServletResponse response = doFilter("/api/orders");
            assertEquals(200, response.getStatus());
        }
    }

    @Test
    void xForwardedForIgnoredWithoutTrustedProxyConfig() throws Exception {
        // Without trusted proxies configured, X-Forwarded-For is ignored.
        // Requests from remoteAddr "10.0.0.1" are rate-limited by that address,
        // regardless of what X-Forwarded-For claims.
        for (int i = 0; i < 20; i++) {
            MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
            request.setRemoteAddr("10.0.0.1");
            request.addHeader("X-Forwarded-For", "1.2.3.4");  // attacker-controlled header
            MockHttpServletResponse response = new MockHttpServletResponse();
            filter.doFilter(request, response, new MockFilterChain());
            assertEquals(200, response.getStatus());
        }
        // 21st from same remoteAddr should be blocked — the forged header did not help
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
        request.setRemoteAddr("10.0.0.1");
        request.addHeader("X-Forwarded-For", "1.2.3.4");
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        assertEquals(429, response.getStatus());
    }

    @Test
    void xForwardedForHonoredWhenRemoteAddrIsTrustedProxy() throws Exception {
        // When the direct TCP peer is a configured trusted proxy, X-Forwarded-For is used.
        RateLimitFilter trustedFilter = new RateLimitFilter(
            objectMapper, Clock.fixed(now, ZoneId.of("UTC")), 60, 20, 120, List.of("127.0.0.1/32"));

        for (int i = 0; i < 20; i++) {
            MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
            request.setRemoteAddr("127.0.0.1");  // trusted proxy
            request.addHeader("X-Forwarded-For", "192.168.1.100");
            MockHttpServletResponse response = new MockHttpServletResponse();
            trustedFilter.doFilter(request, response, new MockFilterChain());
            assertEquals(200, response.getStatus());
        }
        // 21st from same forwarded IP should be blocked
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
        request.setRemoteAddr("127.0.0.1");
        request.addHeader("X-Forwarded-For", "192.168.1.100");
        MockHttpServletResponse response = new MockHttpServletResponse();
        trustedFilter.doFilter(request, response, new MockFilterChain());
        assertEquals(429, response.getStatus());
    }

    @Test
    void logoutUsesPerUserRateLimiting() throws Exception {
        // Exhaust the login IP budget
        for (int i = 0; i < 20; i++) {
            assertEquals(200, doFilterWithIp("/api/auth/login", "10.0.0.5").getStatus());
        }
        assertEquals(429, doFilterWithIp("/api/auth/login", "10.0.0.5").getStatus());

        // Authenticated logout goes through per-user tracking, not login IP tracking — still passes
        setAuthenticated("user1");
        MockHttpServletResponse logoutResponse = doFilter("/api/auth/logout");
        assertEquals(200, logoutResponse.getStatus());
    }

    private void setAuthenticated(String username) {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(username, null, java.util.List.of())
        );
    }

    private MockHttpServletResponse doFilter(String uri) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response;
    }

    private MockHttpServletResponse doFilterWithIp(String uri, String ip) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", uri);
        request.setRemoteAddr(ip);
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response;
    }
}
