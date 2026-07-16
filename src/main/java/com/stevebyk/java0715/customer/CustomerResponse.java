package com.stevebyk.java0715.customer;

import com.stevebyk.java0715.account.UserRegion;

public record CustomerResponse(
        String customerId,
        String fullName,
        UserRegion userRegion,
        String countryCode,
        String phone,
        String email,
        RiskLevel riskLevel,
        CustomerStatus status,
        KycStatus kycStatus,
        KycLevel kycLevel
) {

    static CustomerResponse from(CustomerEntity customer, KycEntity kyc) {
        return new CustomerResponse(customer.getCustomerId(), customer.getFullName(), customer.getUserRegion(),
                customer.getCountryCode(), customer.getPhone(), customer.getEmail(), customer.getRiskLevel(),
                customer.getStatus(), kyc == null ? null : kyc.getStatus(), kyc == null ? null : kyc.getKycLevel());
    }
}
