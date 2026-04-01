package com.pharmaprocure.portal.exception;

import java.util.List;

public class CaptchaRequiredException extends ApiException {

    public CaptchaRequiredException(List<String> details) {
        super(401, "CAPTCHA required", details);
    }
}
