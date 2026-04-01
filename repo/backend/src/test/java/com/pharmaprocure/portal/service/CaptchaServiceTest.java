package com.pharmaprocure.portal.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.pharmaprocure.portal.dto.CaptchaChallengeResponse;
import com.pharmaprocure.portal.exception.CaptchaValidationException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
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

    private int extractExpectedAnswer(String question) {
        String[] tokens = question.replace("?", "").split(" ");
        return Integer.parseInt(tokens[2]) + Integer.parseInt(tokens[4]);
    }
}
