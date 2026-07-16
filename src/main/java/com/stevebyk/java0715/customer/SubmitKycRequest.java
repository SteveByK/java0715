package com.stevebyk.java0715.customer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SubmitKycRequest(
        @NotBlank String documentType,
        @NotBlank String maskedDocumentNo,
        @NotNull KycLevel kycLevel
) {
}
