package com.pharmaprocure.portal.service;

import com.pharmaprocure.portal.exception.ApiException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PasswordPolicyService {

    public void validateOrThrow(String password) {
        List<String> violations = getViolations(password);
        if (!violations.isEmpty()) {
            throw new ApiException(400, "Password policy violation", violations);
        }
    }

    public boolean isValid(String password) {
        return getViolations(password).isEmpty();
    }

    public List<String> getViolations(String password) {
        List<String> violations = new ArrayList<>();
        if (password == null || password.length() < 12) {
            violations.add("Password must contain at least 12 characters");
        }

        int characterClasses = 0;
        if (password != null && password.chars().anyMatch(Character::isLowerCase)) {
            characterClasses++;
        }
        if (password != null && password.chars().anyMatch(Character::isUpperCase)) {
            characterClasses++;
        }
        if (password != null && password.chars().anyMatch(Character::isDigit)) {
            characterClasses++;
        }
        if (password != null && password.chars().anyMatch(ch -> !Character.isLetterOrDigit(ch))) {
            characterClasses++;
        }

        if (characterClasses < 3) {
            violations.add("Password must include at least 3 character classes");
        }
        return violations;
    }
}
