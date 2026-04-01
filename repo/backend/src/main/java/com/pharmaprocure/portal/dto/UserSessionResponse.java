package com.pharmaprocure.portal.dto;

import java.util.Set;

public record UserSessionResponse(
    Long id,
    String username,
    String displayName,
    String organizationCode,
    String role,
    Set<String> permissions
) {
}
