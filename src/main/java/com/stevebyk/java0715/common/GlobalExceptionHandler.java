package com.stevebyk.java0715.common;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
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
     * Converts unexpected failures into a generic system error response.
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleUnknownException(Exception exception) {
        return ApiResponse.failed("SYSTEM_ERROR", exception.getMessage());
    }
}
