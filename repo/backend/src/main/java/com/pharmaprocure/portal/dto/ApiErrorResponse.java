package com.pharmaprocure.portal.dto;

import java.util.List;

public record ApiErrorResponse(
    int code,
    String message,
    List<String> details
) {
}
