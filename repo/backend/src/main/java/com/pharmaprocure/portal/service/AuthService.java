package com.pharmaprocure.portal.service;

import com.pharmaprocure.portal.audit.AuditService;
import com.pharmaprocure.portal.dto.CaptchaChallengeResponse;
import com.pharmaprocure.portal.dto.LoginRequest;
import com.pharmaprocure.portal.dto.LoginResponse;
import com.pharmaprocure.portal.dto.UserSessionResponse;
import com.pharmaprocure.portal.entity.UserEntity;
import com.pharmaprocure.portal.exception.AccountLockedException;
import com.pharmaprocure.portal.exception.CaptchaRequiredException;
import com.pharmaprocure.portal.exception.InvalidCredentialsException;
import com.pharmaprocure.portal.repository.UserRepository;
import com.pharmaprocure.portal.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.Set;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final LoginAttemptPolicyService loginAttemptPolicyService;
    private final CaptchaService captchaService;
    private final AuditService auditService;

    public AuthService(
        UserRepository userRepository,
        AuthenticationManager authenticationManager,
        LoginAttemptPolicyService loginAttemptPolicyService,
        CaptchaService captchaService,
        AuditService auditService
    ) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.loginAttemptPolicyService = loginAttemptPolicyService;
        this.captchaService = captchaService;
        this.auditService = auditService;
    }

    public LoginResponse login(LoginRequest request, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        UserEntity user = userRepository.findWithRoleByUsername(request.username())
            .orElseThrow(() -> invalidCredentials(request.username()));

        if (loginAttemptPolicyService.isLocked(user)) {
            auditService.record("LOGIN_FAILURE", request.username(), "LOCKED");
            throw new AccountLockedException(java.util.List.of("LOCKED_UNTIL:%s".formatted(user.getLockoutUntil())));
        }

        if (loginAttemptPolicyService.requiresCaptcha(user)) {
            if (request.captchaChallengeId() == null || request.captchaAnswer() == null || request.captchaAnswer().isBlank()) {
                throw new CaptchaRequiredException(java.util.List.of("CAPTCHA_REQUIRED"));
            }
            captchaService.validate(request.username(), request.captchaChallengeId(), request.captchaAnswer());
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );

            loginAttemptPolicyService.registerSuccess(user);
            userRepository.save(user);

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);

            HttpSession previous = httpServletRequest.getSession(false);
            if (previous != null) {
                previous.invalidate();
            }
            HttpSession session = httpServletRequest.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

            auditService.record("LOGIN_SUCCESS", request.username());
            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            return new LoginResponse(toSessionResponse(principal), "Login successful");
        } catch (BadCredentialsException ex) {
            loginAttemptPolicyService.registerFailure(user);
            userRepository.save(user);
            auditService.record("LOGIN_FAILURE", request.username(), "BAD_CREDENTIALS");
            if (loginAttemptPolicyService.isLocked(user)) {
                throw new AccountLockedException(java.util.List.of("LOCKED_UNTIL:%s".formatted(user.getLockoutUntil())));
            }
            if (loginAttemptPolicyService.requiresCaptcha(user)) {
                throw new CaptchaRequiredException(java.util.List.of("CAPTCHA_REQUIRED"));
            }
            throw invalidCredentials(request.username());
        }
    }

    public CaptchaChallengeResponse getCaptchaChallenge(String username) {
        UserEntity user = userRepository.findByUsername(username).orElse(null);
        boolean required = user != null && loginAttemptPolicyService.requiresCaptcha(user);
        if (!required) {
            return new CaptchaChallengeResponse(null, "CAPTCHA not required", false);
        }
        return captchaService.issueChallenge(username);
    }

    public UserSessionResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return toSessionResponse(principal);
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            auditService.record("LOGOUT", authentication.getName());
        }
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        response.setHeader("Clear-Site-Data", "\"cookies\"");
    }

    private InvalidCredentialsException invalidCredentials(String username) {
        return new InvalidCredentialsException(java.util.List.of("INVALID_CREDENTIALS"));
    }

    private UserSessionResponse toSessionResponse(UserPrincipal principal) {
        Set<String> permissions = principal.grants().stream().map(grant -> grant.permission().name()).collect(java.util.stream.Collectors.toSet());
        return new UserSessionResponse(principal.id(), principal.username(), principal.displayName(), principal.organizationCode(), principal.role().name(), permissions);
    }
}
