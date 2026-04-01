package com.pharmaprocure.portal.controller;

import com.pharmaprocure.portal.dto.CaptchaChallengeResponse;
import com.pharmaprocure.portal.dto.LoginRequest;
import com.pharmaprocure.portal.dto.LoginResponse;
import com.pharmaprocure.portal.dto.UserSessionResponse;
import com.pharmaprocure.portal.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final CookieCsrfTokenRepository csrfTokenRepository;

    public AuthController(AuthService authService, CookieCsrfTokenRepository csrfTokenRepository) {
        this.authService = authService;
        this.csrfTokenRepository = csrfTokenRepository;
    }

    @GetMapping("/csrf")
    public ResponseEntity<java.util.Map<String, String>> csrf(HttpServletRequest request, HttpServletResponse response) {
        CsrfToken csrfToken = csrfTokenRepository.generateToken(request);
        csrfTokenRepository.saveToken(csrfToken, request, response);
        return ResponseEntity.ok(java.util.Map.of(
            "headerName", csrfToken.getHeaderName(),
            "parameterName", csrfToken.getParameterName(),
            "token", csrfToken.getToken()
        ));
    }

    @GetMapping("/captcha")
    public ResponseEntity<CaptchaChallengeResponse> captcha(@RequestParam String username) {
        return ResponseEntity.ok(authService.getCaptchaChallenge(username));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        return ResponseEntity.ok(authService.login(request, httpServletRequest, httpServletResponse));
    }

    @PostMapping("/logout")
    public ResponseEntity<java.util.Map<String, String>> logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(request, response);
        return ResponseEntity.ok(java.util.Map.of("message", "Logged out successfully"));
    }

    @GetMapping("/me")
    public ResponseEntity<UserSessionResponse> currentUser() {
        return ResponseEntity.ok(authService.getCurrentUser());
    }
}
