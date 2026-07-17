package com.stevebyk.java0715.auth;

/**
 * Immutable login audit result values persisted for security review.
 */
public enum LoginResult {
    SUCCESS,
    BAD_CREDENTIALS,
    LOCKED,
    DISABLED
}
