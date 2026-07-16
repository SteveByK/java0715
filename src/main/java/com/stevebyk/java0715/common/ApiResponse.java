package com.stevebyk.java0715.common;

import java.time.Instant;

public record ApiResponse<T>(
        String code,
        String message,
        T data,
        Instant timestamp
) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>("SUCCESS", "success", data, Instant.now());
    }

    public static <T> ApiResponse<T> failed(String code, String message) {
        return new ApiResponse<>(code, message, null, Instant.now());
    }
}
