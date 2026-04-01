package com.pharmaprocure.portal.exception;

import java.util.List;

public class InvalidCredentialsException extends ApiException {

    public InvalidCredentialsException(List<String> details) {
        super(401, "Invalid credentials", details);
    }
}
