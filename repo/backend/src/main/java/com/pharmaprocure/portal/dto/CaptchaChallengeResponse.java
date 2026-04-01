package com.pharmaprocure.portal.dto;

public record CaptchaChallengeResponse(
    String challengeId,
    String question,
    boolean required
) {
}
