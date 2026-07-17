package com.stevebyk.java0715.auth;

/**
 * Identity lifecycle status used before accepting login or refresh requests.
 */
public enum AuthUserStatus {
    ACTIVE,
    DISABLED,
    LOCKED
}
