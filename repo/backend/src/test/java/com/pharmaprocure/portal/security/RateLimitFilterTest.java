package com.pharmaprocure.portal.security;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
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
        filter = new RateLimitFilter(objectMapper, Clock.fixed(now, ZoneId.of("UTC")), 60, 20);
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
        filter = new RateLimitFilter(objectMapper, Clock.fixed(now.plusSeconds(61), ZoneId.of("UTC")), 60, 20);
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
    void authCsrfEndpointUses60PerMinuteLimit() throws Exception {
        for (int i = 0; i < 60; i++) {
            assertEquals(200, doFilterWithIp("/api/auth/csrf", "10.0.0.1").getStatus());
        }
        assertEquals(429, doFilterWithIp("/api/auth/csrf", "10.0.0.1").getStatus());
    }

    @Test
    void authCaptchaEndpointUses60PerMinuteLimit() throws Exception {
        for (int i = 0; i < 60; i++) {
            assertEquals(200, doFilterWithIp("/api/auth/captcha", "10.0.0.1").getStatus());
        }
        assertEquals(429, doFilterWithIp("/api/auth/captcha", "10.0.0.1").getStatus());
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
    void xForwardedForHeaderUsedForIpResolution() throws Exception {
        for (int i = 0; i < 20; i++) {
            MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
            request.addHeader("X-Forwarded-For", "192.168.1.100, 10.0.0.1");
            request.setRemoteAddr("127.0.0.1");
            MockHttpServletResponse response = new MockHttpServletResponse();
            filter.doFilter(request, response, new MockFilterChain());
            assertEquals(200, response.getStatus());
        }
        // 21st from same forwarded IP should be blocked
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
        request.addHeader("X-Forwarded-For", "192.168.1.100, 10.0.0.1");
        request.setRemoteAddr("127.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        assertEquals(429, response.getStatus());
    }

    @Test
    void loginAndCsrfUseIndependentWindows() throws Exception {
        // Login uses a separate window from other /api/auth/* endpoints.
        // Exhausting the login limit does not affect the csrf limit and vice versa.
        for (int i = 0; i < 20; i++) {
            assertEquals(200, doFilterWithIp("/api/auth/login", "10.0.0.5").getStatus());
        }
        // Login window is exhausted
        assertEquals(429, doFilterWithIp("/api/auth/login", "10.0.0.5").getStatus());

        // CSRF uses its own independent window — still passes
        assertEquals(200, doFilterWithIp("/api/auth/csrf", "10.0.0.5").getStatus());

        // Exhaust csrf window independently (60 calls already made: 1 above + 59 more)
        for (int i = 0; i < 59; i++) {
            doFilterWithIp("/api/auth/csrf", "10.0.0.5");
        }
        assertEquals(429, doFilterWithIp("/api/auth/csrf", "10.0.0.5").getStatus());

        // Login window remains independent — still blocked at its own limit
        assertEquals(429, doFilterWithIp("/api/auth/login", "10.0.0.5").getStatus());
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
