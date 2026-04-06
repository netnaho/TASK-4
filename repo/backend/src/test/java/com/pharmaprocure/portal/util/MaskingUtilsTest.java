package com.pharmaprocure.portal.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MaskingUtilsTest {

    // ── mask() ───────────────────────────────────────────────────────────────

    @Test
    void maskNullReturnsStars() {
        assertEquals("****", MaskingUtils.mask(null));
    }

    @Test
    void maskBlankReturnsStars() {
        assertEquals("****", MaskingUtils.mask("   "));
    }

    @Test
    void maskShortValueRetainsLastCharsWithMinimumStarPrefix() {
        // "ab" → visible=2, suffix="ab", stars=max(0,4)=4 → "****ab"
        assertEquals("****ab", MaskingUtils.mask("ab"));
    }

    @Test
    void maskLongValueRetainsLast4Chars() {
        // "abcdefghij" (10 chars) → visible=4, suffix="ghij", stars=6 → "******ghij"
        String result = MaskingUtils.mask("abcdefghij");
        assertTrue(result.endsWith("ghij"), "Last 4 chars should be visible: " + result);
        assertTrue(result.startsWith("****"), "Prefix should be stars: " + result);
        assertFalse(result.contains("abcdef"), "First chars should not appear: " + result);
    }

    @Test
    void maskExactly4CharsRetainsAll4WithMinimumStarPrefix() {
        // "abcd" → visible=4, suffix="abcd", stars=max(0,4)=4 → "****abcd"
        assertEquals("****abcd", MaskingUtils.mask("abcd"));
    }

    @Test
    void maskDoesNotExposeUsername() {
        String masked = MaskingUtils.mask("buyer1");
        assertFalse(masked.contains("buyer"), "Username should be partially hidden: " + masked);
    }

    // ── sanitizeText() ───────────────────────────────────────────────────────

    @Test
    void sanitizeTextNullReturnsNull() {
        assertNull(MaskingUtils.sanitizeText(null));
    }

    @Test
    void sanitizeTextBlankReturnsBlank() {
        assertEquals("  ", MaskingUtils.sanitizeText("  "));
    }

    @Test
    void sanitizeTextPlainTextUnchanged() {
        assertEquals("Order approved successfully", MaskingUtils.sanitizeText("Order approved successfully"));
    }

    @Test
    void sanitizeTextMasksEmailAddress() {
        String result = MaskingUtils.sanitizeText("Contact admin@example.com for help");
        assertFalse(result.contains("admin@example.com"), "Email should be masked: " + result);
        assertTrue(result.contains("[masked-email]"), "Should use masked-email placeholder: " + result);
    }

    @Test
    void sanitizeTextMasksLabeledUsername() {
        // Pattern requires label[:=]value — space-only separator does not match
        String result = MaskingUtils.sanitizeText("INVALID_CREDENTIALS_FOR=buyer1 attempt logged");
        assertFalse(result.contains("buyer1"), "Username should be masked: " + result);
        assertTrue(result.contains("****"), "Should contain masking stars: " + result);
    }

    @Test
    void sanitizeTextMasksUsernameEqualsPattern() {
        String result = MaskingUtils.sanitizeText("username=admin1 login failed");
        assertFalse(result.contains("admin1"), "Username value should be masked: " + result);
        assertTrue(result.contains("****"), "Should contain masking stars: " + result);
    }

    @Test
    void sanitizeTextMasksPrincipalLabel() {
        String result = MaskingUtils.sanitizeText("principal: quality1 accessed document");
        assertFalse(result.contains("quality1"), "Principal should be masked: " + result);
        assertTrue(result.contains("****"), "Should contain masking stars: " + result);
    }

    @Test
    void sanitizeTextMasksInvalidCredentialsForPattern() {
        String result = MaskingUtils.sanitizeText("INVALID_CREDENTIALS_FOR=finance1");
        assertFalse(result.contains("finance1"), "Username in credentials error should be masked: " + result);
        assertTrue(result.contains("****"), "Should contain masking stars: " + result);
    }

    @Test
    void sanitizeTextPreservesNonSensitiveContent() {
        String input = "Order ORD-12345 moved to APPROVED status";
        assertEquals(input, MaskingUtils.sanitizeText(input));
    }

    @Test
    void sanitizeTextMasksMultipleSensitiveTokens() {
        String result = MaskingUtils.sanitizeText("user=alice and email=bob@test.com failed");
        assertFalse(result.contains("alice"), "alice should be masked");
        assertFalse(result.contains("bob@test.com"), "email should be masked");
    }
}
