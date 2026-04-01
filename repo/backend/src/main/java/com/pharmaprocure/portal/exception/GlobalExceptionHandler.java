package com.pharmaprocure.portal.exception;

import com.pharmaprocure.portal.dto.ApiErrorResponse;
import com.pharmaprocure.portal.util.MaskingUtils;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({AccessDeniedException.class, AuthorizationDeniedException.class})
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(Exception ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(new ApiErrorResponse(403, "Access denied", List.of("FORBIDDEN")));
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorResponse> handleApiException(ApiException ex) {
        String safeMessage = MaskingUtils.sanitizeText(ex.getMessage());
        List<String> safeDetails = ex.getDetails() == null
            ? List.of()
            : ex.getDetails().stream().map(MaskingUtils::sanitizeText).toList();
        return ResponseEntity.status(ex.getCode())
            .body(new ApiErrorResponse(ex.getCode(), safeMessage, safeDetails));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex) {
        return ResponseEntity.badRequest()
            .body(new ApiErrorResponse(400, "File exceeds maximum size", List.of("MAX_25_MB")));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult()
            .getAllErrors()
            .stream()
            .map(error -> {
                if (error instanceof FieldError fieldError) {
                    return MaskingUtils.sanitizeText(fieldError.getField() + ": " + fieldError.getDefaultMessage());
                }
                return MaskingUtils.sanitizeText(error.getDefaultMessage());
            })
            .toList();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ApiErrorResponse(400, "Validation failed", details));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        List<String> details = ex.getConstraintViolations().stream()
            .map(v -> MaskingUtils.sanitizeText(v.getPropertyPath() + ": " + v.getMessage()))
            .toList();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ApiErrorResponse(400, "Validation failed", details));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneralException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ApiErrorResponse(500, "Unexpected server error", List.of("INTERNAL_ERROR")));
    }
}
