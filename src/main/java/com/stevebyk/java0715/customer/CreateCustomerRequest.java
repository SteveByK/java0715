package com.stevebyk.java0715.customer;

import com.stevebyk.java0715.account.UserRegion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCustomerRequest(
        @NotBlank String customerId,
        @NotBlank String fullName,
        @NotNull UserRegion userRegion,
        @NotBlank String countryCode,
        String phone,
        String email
) {
}
