package com.pharmaprocure.portal.validation;

import org.springframework.stereotype.Component;
import com.pharmaprocure.portal.service.PasswordPolicyService;

@Component
public class PasswordPolicyValidator {

    private final PasswordPolicyService passwordPolicyService;

    public PasswordPolicyValidator(PasswordPolicyService passwordPolicyService) {
        this.passwordPolicyService = passwordPolicyService;
    }

    public boolean isValid(String password) {
        return passwordPolicyService.isValid(password);
    }
}
