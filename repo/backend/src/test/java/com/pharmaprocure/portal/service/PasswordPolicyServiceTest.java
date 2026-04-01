package com.pharmaprocure.portal.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PasswordPolicyServiceTest {

    private final PasswordPolicyService passwordPolicyService = new PasswordPolicyService();

    @Test
    void acceptsPasswordWithMinimumLengthAndThreeClasses() {
        assertTrue(passwordPolicyService.isValid("PortalAccess2026!"));
    }

    @Test
    void rejectsPasswordThatIsTooShort() {
        assertFalse(passwordPolicyService.isValid("Short1!"));
    }

    @Test
    void rejectsPasswordWithFewerThanThreeCharacterClasses() {
        assertFalse(passwordPolicyService.isValid("alllowercasepassword"));
    }
}
