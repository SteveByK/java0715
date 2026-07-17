package com.stevebyk.java0715.transfer;

/**
 * Shared transaction lifecycle state for transfer, remittance and reversal orders.
 */
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
