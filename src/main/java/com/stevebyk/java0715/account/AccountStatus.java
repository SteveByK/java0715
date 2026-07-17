package com.stevebyk.java0715.account;

/**
 * Account lifecycle state used to gate money movement.
 */
public enum AccountStatus {
    ACTIVE,
    FROZEN,
    CLOSED
}
