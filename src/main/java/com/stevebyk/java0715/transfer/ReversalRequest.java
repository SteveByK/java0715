package com.stevebyk.java0715.transfer;

import jakarta.validation.constraints.NotBlank;

public record ReversalRequest(
        @NotBlank String requestId,
        @NotBlank String reason
) {
}
