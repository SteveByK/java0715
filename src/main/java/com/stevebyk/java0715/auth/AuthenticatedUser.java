package com.stevebyk.java0715.auth;

import java.util.List;

/**
 * Security principal placed into Spring Security after a JWT is validated.
 */
public record AuthenticatedUser(
        String userId,
        String username,
        String displayName,
        List<String> roles,
        List<String> permissions
) {
}
