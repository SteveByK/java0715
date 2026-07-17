package com.stevebyk.java0715.auth;

import java.util.List;

/**
 * Authentication response returned after login or refresh-token rotation.
 */
public record LoginResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresInSeconds,
        String userId,
        String username,
        String displayName,
        List<String> roles,
        List<String> permissions
) {
}
