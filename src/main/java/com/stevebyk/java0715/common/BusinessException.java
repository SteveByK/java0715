package com.stevebyk.java0715.common;

/**
 * Business-level exception with a stable error code for API responses.
 */
public class BusinessException extends RuntimeException {

    private final String code;

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * Returns the stable API error code associated with this business failure.
     */
    public String code() {
        return code;
    }
}
