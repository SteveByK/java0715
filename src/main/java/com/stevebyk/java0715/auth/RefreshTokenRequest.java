package com.stevebyk.java0715.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * Refresh-token payload used to rotate credentials.
 */
public record RefreshTokenRequest(@NotBlank String refreshToken) {
}
