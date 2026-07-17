package com.stevebyk.java0715.transfer;

import java.math.BigDecimal;

/**
 * Read model returned after a transfer reversal command.
 */
public record ReversalResponse(
        String reversalNo,
        String originalOrderNo,
        String requestId,
        BigDecimal amount,
        String currency,
        TransactionStatus status,
        String reason
) {

    static ReversalResponse from(ReversalOrderEntity entity) {
        return new ReversalResponse(entity.getReversalNo(), entity.getOriginalOrderNo(), entity.getRequestId(),
                entity.getAmount(), entity.getCurrency(), entity.getStatus(), entity.getReason());
    }
}
