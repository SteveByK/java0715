package com.stevebyk.java0715.auth;

import java.util.List;

/**
 * Current authenticated user profile exposed to the frontend shell.
 */
public record CurrentUserResponse(
        String userId,
        String username,
        String displayName,
        List<String> roles,
        List<String> permissions
) {

    static CurrentUserResponse from(AuthenticatedUser user) {
        return new CurrentUserResponse(
                user.userId(),
                user.username(),
                user.displayName(),
                user.roles(),
                user.permissions());
    }
}
