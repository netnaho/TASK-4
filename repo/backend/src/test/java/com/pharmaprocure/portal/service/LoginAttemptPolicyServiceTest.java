package com.pharmaprocure.portal.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pharmaprocure.portal.entity.UserEntity;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class LoginAttemptPolicyServiceTest {

    private final LoginAttemptPolicyService service = new LoginAttemptPolicyService(
        Clock.fixed(Instant.parse("2026-03-29T00:00:00Z"), ZoneOffset.UTC)
    );

    @Test
    void requiresCaptchaAfterThreeFailures() {
        UserEntity user = new UserEntity();
        service.registerFailure(user);
        service.registerFailure(user);
        assertFalse(service.requiresCaptcha(user));

        service.registerFailure(user);
        assertTrue(service.requiresCaptcha(user));
    }

    @Test
    void locksAccountAfterFiveFailures() {
        UserEntity user = new UserEntity();
        for (int i = 0; i < 5; i++) {
            service.registerFailure(user);
        }
        assertTrue(service.isLocked(user));
    }

    @Test
    void resetsFailureStateAfterSuccess() {
        UserEntity user = new UserEntity();
        for (int i = 0; i < 5; i++) {
            service.registerFailure(user);
        }

        service.registerSuccess(user);

        assertFalse(service.requiresCaptcha(user));
        assertFalse(service.isLocked(user));
        assertNull(user.getLockoutUntil());
    }
}
