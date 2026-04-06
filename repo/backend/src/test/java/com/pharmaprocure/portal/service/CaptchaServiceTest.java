package com.pharmaprocure.portal.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.pharmaprocure.portal.dto.CaptchaChallengeResponse;
import com.pharmaprocure.portal.exception.CaptchaValidationException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CaptchaServiceTest {

    private final CaptchaService captchaService = new CaptchaService(
        Clock.fixed(Instant.parse("2026-03-29T00:00:00Z"), ZoneOffset.UTC)
    );

    @Test
    void validatesCorrectChallengeAnswer() {
        CaptchaChallengeResponse challenge = captchaService.issueChallenge("buyer1");
        int expected = extractExpectedAnswer(challenge.question());

        assertDoesNotThrow(() -> captchaService.validate("buyer1", challenge.challengeId(), Integer.toString(expected)));
    }

    @Test
    void rejectsWrongCaptchaAnswer() {
        CaptchaChallengeResponse challenge = captchaService.issueChallenge("buyer1");
        assertThrows(CaptchaValidationException.class, () -> captchaService.validate("buyer1", challenge.challengeId(), "999"));
    }

    @Test
    void rejectsReplayAfterSuccessfulValidation() {
        CaptchaChallengeResponse challenge = captchaService.issueChallenge("buyer2");
        int answer = extractExpectedAnswer(challenge.question());
        captchaService.validate("buyer2", challenge.challengeId(), Integer.toString(answer));
        // Second use of the same challenge must fail
        CaptchaValidationException ex = assertThrows(CaptchaValidationException.class,
            () -> captchaService.validate("buyer2", challenge.challengeId(), Integer.toString(answer)));
        assertEquals("CAPTCHA_EXPIRED", ex.getDetails().get(0));
    }

    @Test
    void exceedingMaxAttemptsInvalidatesChallenge() {
        CaptchaChallengeResponse challenge = captchaService.issueChallenge("buyer3");
        // Three wrong answers consume the attempt budget
        for (int i = 0; i < 3; i++) {
            assertThrows(CaptchaValidationException.class,
                () -> captchaService.validate("buyer3", challenge.challengeId(), "999"));
        }
        // Fourth attempt — even with the correct answer — must be rejected
        int correct = extractExpectedAnswer(challenge.question());
        CaptchaValidationException ex = assertThrows(CaptchaValidationException.class,
            () -> captchaService.validate("buyer3", challenge.challengeId(), Integer.toString(correct)));
        assertEquals("CAPTCHA_MAX_ATTEMPTS_EXCEEDED", ex.getDetails().get(0));
    }

    @Test
    void rejectsExpiredChallenge() {
        CaptchaService expired = new CaptchaService(Clock.fixed(Instant.parse("2026-03-29T00:00:00Z"), ZoneOffset.UTC));
        CaptchaChallengeResponse challenge = expired.issueChallenge("buyer4");
        // Validate with a clock that is 301 seconds in the future (past the 300 s window)
        CaptchaService future = new CaptchaService(Clock.fixed(Instant.parse("2026-03-29T00:05:01Z"), ZoneOffset.UTC));
        CaptchaValidationException ex = assertThrows(CaptchaValidationException.class,
            () -> future.validate("buyer4", challenge.challengeId(), "1"));
        assertEquals("CAPTCHA_EXPIRED", ex.getDetails().get(0));
    }

    @Test
    void rejectsUsernameMismatch() {
        CaptchaChallengeResponse challenge = captchaService.issueChallenge("buyer5");
        int answer = extractExpectedAnswer(challenge.question());
        CaptchaValidationException ex = assertThrows(CaptchaValidationException.class,
            () -> captchaService.validate("OTHER_USER", challenge.challengeId(), Integer.toString(answer)));
        assertEquals("CAPTCHA_INVALID", ex.getDetails().get(0));
    }

    @Test
    void operandsAreRandomAcrossIssuances() {
        // Run 20 issuances and assert that operand pairs are not all identical —
        // with a uniform distribution over 8×7 = 56 combinations, collisions are
        // statistically extremely unlikely in 20 draws.
        Set<String> questions = new HashSet<>();
        for (int i = 0; i < 20; i++) {
            CaptchaChallengeResponse challenge = captchaService.issueChallenge("user" + i);
            questions.add(challenge.question());
        }
        assertNotEquals(1, questions.size(), "All 20 challenges produced identical operands — SecureRandom may not be in use");
    }

    @Test
    void issuingNewChallengeInvalidatesPreviousOneForSameUser() {
        CaptchaChallengeResponse first = captchaService.issueChallenge("buyer6");
        captchaService.issueChallenge("buyer6"); // second issuance should invalidate first
        int answer = extractExpectedAnswer(first.question());
        CaptchaValidationException ex = assertThrows(CaptchaValidationException.class,
            () -> captchaService.validate("buyer6", first.challengeId(), Integer.toString(answer)));
        assertEquals("CAPTCHA_EXPIRED", ex.getDetails().get(0));
    }

    private int extractExpectedAnswer(String question) {
        String[] tokens = question.replace("?", "").split(" ");
        return Integer.parseInt(tokens[2]) + Integer.parseInt(tokens[4]);
    }
}
