package com.pharmaprocure.portal.service;

import com.pharmaprocure.portal.dto.CaptchaChallengeResponse;
import com.pharmaprocure.portal.exception.CaptchaValidationException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class CaptchaService {

    private static final int MAX_ATTEMPTS = 3;
    private static final int EXPIRY_SECONDS = 300;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final Map<String, CaptchaChallenge> challenges = new ConcurrentHashMap<>();
    private final Clock clock;

    public CaptchaService() {
        this(Clock.systemUTC());
    }

    CaptchaService(Clock clock) {
        this.clock = clock;
    }

    public CaptchaChallengeResponse issueChallenge(String username) {
        // Invalidate any outstanding challenge for this username before issuing a new one
        challenges.entrySet().removeIf(e -> e.getValue().username().equalsIgnoreCase(username));

        int left = 2 + SECURE_RANDOM.nextInt(8);   // 2–9 inclusive
        int right = 3 + SECURE_RANDOM.nextInt(7);  // 3–9 inclusive
        String challengeId = UUID.randomUUID().toString();
        challenges.put(challengeId, new CaptchaChallenge(
            username.toLowerCase(),
            Integer.toString(left + right),
            clock.instant().plusSeconds(EXPIRY_SECONDS),
            0
        ));
        return new CaptchaChallengeResponse(challengeId, "What is %d + %d?".formatted(left, right), true);
    }

    public void validate(String username, String challengeId, String answer) {
        CaptchaChallenge challenge = challenges.get(challengeId);
        if (challenge == null || challenge.expiresAt().isBefore(clock.instant())) {
            challenges.remove(challengeId);
            throw new CaptchaValidationException(List.of("CAPTCHA_EXPIRED"));
        }
        if (challenge.attempts() >= MAX_ATTEMPTS) {
            challenges.remove(challengeId);
            throw new CaptchaValidationException(List.of("CAPTCHA_MAX_ATTEMPTS_EXCEEDED"));
        }
        if (!challenge.username().equalsIgnoreCase(username) || answer == null || !challenge.answer().equals(answer.trim())) {
            challenges.put(challengeId, new CaptchaChallenge(
                challenge.username(), challenge.answer(), challenge.expiresAt(), challenge.attempts() + 1
            ));
            throw new CaptchaValidationException(List.of("CAPTCHA_INVALID"));
        }
        // Correct answer — consume challenge so it cannot be replayed
        challenges.remove(challengeId);
    }

    private record CaptchaChallenge(String username, String answer, Instant expiresAt, int attempts) {
    }
}
