package com.pharmaprocure.portal.exception;

import java.util.List;

public class AccountLockedException extends ApiException {

    public AccountLockedException(List<String> details) {
        super(423, "Account locked", details);
    }
}
