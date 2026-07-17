package com.stevebyk.java0715.common;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
/**
 * Maps validation, business and unexpected exceptions to the standard API response shape.
 */
public class GlobalExceptionHandler {

    /**
     * Converts expected domain and application failures into client-visible errors.
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleBusinessException(BusinessException exception) {
        return ApiResponse.failed(exception.code(), exception.getMessage());
    }

    /**
     * Converts bean validation failures into standard validation responses.
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleValidationException(Exception exception) {
        return ApiResponse.failed("VALIDATION_ERROR", exception.getMessage());
    }

    /**
     * Converts method-level RBAC denials into a stable forbidden response.
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResponse<Void> handleAccessDeniedException(AccessDeniedException exception) {
        return ApiResponse.failed("FORBIDDEN", "permission denied");
    }

    /**
     * Converts authentication failures into a stable unauthorized response.
     */
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResponse<Void> handleAuthenticationException(AuthenticationException exception) {
        return ApiResponse.failed("UNAUTHORIZED", "authentication is required");
    }

    /**
     * Converts unexpected failures into a generic system error response.
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleUnknownException(Exception exception) {
        return ApiResponse.failed("SYSTEM_ERROR", exception.getMessage());
    }
}
