package com.pharmaprocure.portal.service;

import com.pharmaprocure.portal.dto.CaptchaChallengeResponse;
import com.pharmaprocure.portal.exception.CaptchaValidationException;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class CaptchaService {

    private final Map<String, CaptchaChallenge> challenges = new ConcurrentHashMap<>();
    private final Clock clock;

    public CaptchaService() {
        this(Clock.systemUTC());
    }

    CaptchaService(Clock clock) {
        this.clock = clock;
    }

    public CaptchaChallengeResponse issueChallenge(String username) {
        int left = 2 + Math.abs(username.hashCode() % 8);
        int right = 3 + Math.abs((username.hashCode() / 7) % 7);
        String challengeId = UUID.randomUUID().toString();
        challenges.put(challengeId, new CaptchaChallenge(username.toLowerCase(), Integer.toString(left + right), clock.instant().plusSeconds(300)));
        return new CaptchaChallengeResponse(challengeId, "What is %d + %d?".formatted(left, right), true);
    }

    public void validate(String username, String challengeId, String answer) {
        CaptchaChallenge challenge = challenges.get(challengeId);
        if (challenge == null || challenge.expiresAt().isBefore(clock.instant())) {
            throw new CaptchaValidationException(java.util.List.of("CAPTCHA_EXPIRED"));
        }
        if (!challenge.username().equalsIgnoreCase(username) || answer == null || !challenge.answer().equals(answer.trim())) {
            throw new CaptchaValidationException(java.util.List.of("CAPTCHA_INVALID"));
        }
        challenges.remove(challengeId);
    }

    private record CaptchaChallenge(String username, String answer, Instant expiresAt) {
    }
}
