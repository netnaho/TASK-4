package com.pharmaprocure.portal.exception;

import java.util.List;

public class CaptchaValidationException extends ApiException {

    public CaptchaValidationException(List<String> details) {
        super(400, "CAPTCHA validation failed", details);
    }
}
