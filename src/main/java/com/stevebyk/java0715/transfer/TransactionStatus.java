package com.stevebyk.java0715.transfer;

public enum TransactionStatus {
    CREATED,
    RISK_REJECTED,
    PROCESSING,
    DEBIT_SUCCESS,
    CREDIT_SUCCESS,
    SUCCESS,
    FAILED,
    COMPENSATING,
    REVERSED,
    UNKNOWN
}
