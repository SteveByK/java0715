package com.stevebyk.java0715.transfer;

import java.math.BigDecimal;

public record TransferResponse(
        String orderNo,
        String requestId,
        String fromAccountNo,
        String toAccountNo,
        BigDecimal amount,
        BigDecimal fee,
        String currency,
        TransactionStatus status,
        String riskCode,
        String failureReason
) {

    public static TransferResponse from(TransferOrderEntity entity) {
        return new TransferResponse(entity.getOrderNo(), entity.getRequestId(), entity.getFromAccountNo(),
                entity.getToAccountNo(), entity.getAmount(), entity.getFee(), entity.getCurrency(), entity.getStatus(),
                entity.getRiskCode(), entity.getFailureReason());
    }
}
