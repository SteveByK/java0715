package com.stevebyk.java0715.remittance;

import com.stevebyk.java0715.transfer.TransactionStatus;
import java.math.BigDecimal;

public record RemittanceResponse(
        String orderNo,
        String requestId,
        String senderAccountNo,
        String receiverAccountNo,
        BigDecimal sourceAmount,
        BigDecimal exchangeRate,
        BigDecimal fee,
        BigDecimal targetAmount,
        String quoteId,
        String feeRuleCode,
        String rateCode,
        String sourceCurrency,
        String targetCurrency,
        String destinationCountry,
        TransactionStatus status,
        String riskCode,
        String failureReason
) {

    public static RemittanceResponse from(RemittanceOrderEntity entity) {
        return new RemittanceResponse(entity.getOrderNo(), entity.getRequestId(), entity.getSenderAccountNo(),
                entity.getReceiverAccountNo(), entity.getSourceAmount(), entity.getExchangeRate(), entity.getFee(),
                entity.getTargetAmount(), entity.getQuoteId(), entity.getFeeRuleCode(), entity.getRateCode(),
                entity.getSourceCurrency(), entity.getTargetCurrency(), entity.getDestinationCountry(),
                entity.getStatus(), entity.getRiskCode(), entity.getFailureReason());
    }
}
