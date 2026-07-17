package com.stevebyk.java0715.transfer;

import jakarta.validation.constraints.NotBlank;

/**
 * Command payload for reversing a successful domestic transfer.
 */
public record ReversalRequest(
        @NotBlank String requestId,
        @NotBlank String reason
) {
}
