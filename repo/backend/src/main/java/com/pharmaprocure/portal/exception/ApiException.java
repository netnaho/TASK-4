package com.pharmaprocure.portal.exception;

import java.util.List;

public class ApiException extends RuntimeException {

    private final int code;
    private final List<String> details;

    public ApiException(int code, String message, List<String> details) {
        super(message);
        this.code = code;
        this.details = details;
    }

    public int getCode() {
        return code;
    }

    public List<String> getDetails() {
        return details;
    }
}
