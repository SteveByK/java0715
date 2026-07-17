package com.stevebyk.java0715.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * Username and password credential submitted to the login endpoint.
 */
public record LoginRequest(
        @NotBlank String username,
        @NotBlank String password
) {
}
