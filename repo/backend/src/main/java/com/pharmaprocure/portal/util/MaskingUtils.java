package com.pharmaprocure.portal.util;

import java.util.regex.Pattern;

public final class MaskingUtils {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("(?i)\\b[\\w.%+-]+@[\\w.-]+\\.[a-z]{2,}\\b");
    private static final Pattern LABELED_IDENTIFIER_PATTERN = Pattern.compile("(?i)\\b(username|user|principal|actor|owner|email)(\\s*[:=]\\s*)([^,;\\s]+)");
    private static final Pattern INVALID_CREDENTIALS_FOR_PATTERN = Pattern.compile("(?i)(INVALID_CREDENTIALS_FOR\\s*[:=]\\s*)([^,;\\s]+)");

    private MaskingUtils() {
    }

    public static String mask(String raw) {
        if (raw == null || raw.isBlank()) {
            return "****";
        }
        int visible = Math.min(4, raw.length());
        String suffix = raw.substring(raw.length() - visible);
        return "*".repeat(Math.max(raw.length() - visible, 4)) + suffix;
    }

    public static String sanitizeText(String raw) {
        if (raw == null || raw.isBlank()) {
            return raw;
        }
        String sanitized = EMAIL_PATTERN.matcher(raw).replaceAll("[masked-email]");
        sanitized = LABELED_IDENTIFIER_PATTERN.matcher(sanitized).replaceAll("$1$2****");
        sanitized = INVALID_CREDENTIALS_FOR_PATTERN.matcher(sanitized).replaceAll("$1****");
        return sanitized;
    }
}
