package com.pharmaprocure.portal.dto;

public record LoginResponse(
    UserSessionResponse user,
    String message
) {
}
