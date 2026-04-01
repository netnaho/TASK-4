package com.pharmaprocure.portal.service;

import com.pharmaprocure.portal.entity.UserEntity;
import java.time.Clock;
import java.time.OffsetDateTime;
import org.springframework.stereotype.Service;

@Service
public class LoginAttemptPolicyService {

    public static final int CAPTCHA_THRESHOLD = 3;
    public static final int LOCKOUT_THRESHOLD = 5;
    public static final long LOCKOUT_MINUTES = 15;

    private final Clock clock;

    public LoginAttemptPolicyService() {
        this(Clock.systemUTC());
    }

    LoginAttemptPolicyService(Clock clock) {
        this.clock = clock;
    }

    public boolean isLocked(UserEntity user) {
        return user.getLockoutUntil() != null && user.getLockoutUntil().isAfter(now());
    }

    public boolean requiresCaptcha(UserEntity user) {
        return user.getFailedLoginAttempts() >= CAPTCHA_THRESHOLD;
    }

    public void registerFailure(UserEntity user) {
        user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
        user.setLastFailedLoginAt(now());
        if (user.getFailedLoginAttempts() >= LOCKOUT_THRESHOLD) {
            user.setLockoutUntil(now().plusMinutes(LOCKOUT_MINUTES));
        }
    }

    public void registerSuccess(UserEntity user) {
        user.setFailedLoginAttempts(0);
        user.setLockoutUntil(null);
        user.setLastLoginAt(now());
    }

    private OffsetDateTime now() {
        return OffsetDateTime.now(clock);
    }
}
