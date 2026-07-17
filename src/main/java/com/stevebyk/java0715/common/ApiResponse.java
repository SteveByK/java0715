package com.stevebyk.java0715.common;

import java.time.Instant;

/**
 * Standard response wrapper returned by all HTTP APIs.
 */
public record ApiResponse<T>(
        String code,
        String message,
        T data,
        Instant timestamp
) {

    /**
     * Builds a successful API response.
     */
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>("SUCCESS", "success", data, Instant.now());
    }

    /**
     * Builds a failed API response with a stable business or system code.
     */
    public static <T> ApiResponse<T> failed(String code, String message) {
        return new ApiResponse<>(code, message, null, Instant.now());
    }
}
