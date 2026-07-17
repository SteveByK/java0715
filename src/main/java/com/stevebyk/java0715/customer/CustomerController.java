package com.stevebyk.java0715.customer;

import com.stevebyk.java0715.common.ApiResponse;
import com.stevebyk.java0715.common.ddd.InboundAdapter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/customers")
@Tag(name = "Customer and KYC", description = "Customer profile and KYC lifecycle commands")
@InboundAdapter
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Operation(summary = "Create customer", description = "Creates a customer profile used by account and KYC workflows.")
    @PostMapping
    @PreAuthorize("hasAuthority('customer:create')")
    public ApiResponse<CustomerResponse> create(@Valid @RequestBody CreateCustomerRequest request) {
        return ApiResponse.ok(customerService.createCustomer(request));
    }

    @Operation(summary = "Get customer", description = "Returns customer profile and KYC summary.")
    @GetMapping("/{customerId}")
    @PreAuthorize("hasAuthority('customer:read')")
    public ApiResponse<CustomerResponse> get(@PathVariable String customerId) {
        return ApiResponse.ok(customerService.getCustomer(customerId));
    }

    @Operation(summary = "Submit KYC", description = "Creates or updates the customer's pending KYC record.")
    @PutMapping("/{customerId}/kyc")
    @PreAuthorize("hasAuthority('kyc:submit')")
    public ApiResponse<CustomerResponse> submitKyc(@PathVariable String customerId,
                                                   @Valid @RequestBody SubmitKycRequest request) {
        return ApiResponse.ok(customerService.submitKyc(customerId, request));
    }

    @Operation(summary = "Review KYC", description = "Approves or rejects a submitted KYC record.")
    @PostMapping("/{customerId}/kyc/review")
    @PreAuthorize("hasAuthority('kyc:review')")
    public ApiResponse<CustomerResponse> reviewKyc(@PathVariable String customerId,
                                                   @Valid @RequestBody ReviewKycRequest request) {
        return ApiResponse.ok(customerService.reviewKyc(customerId, request));
    }
}
