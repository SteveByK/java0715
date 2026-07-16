package com.stevebyk.java0715.customer;

import com.stevebyk.java0715.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    public ApiResponse<CustomerResponse> create(@Valid @RequestBody CreateCustomerRequest request) {
        return ApiResponse.ok(customerService.createCustomer(request));
    }

    @GetMapping("/{customerId}")
    public ApiResponse<CustomerResponse> get(@PathVariable String customerId) {
        return ApiResponse.ok(customerService.getCustomer(customerId));
    }

    @PutMapping("/{customerId}/kyc")
    public ApiResponse<CustomerResponse> submitKyc(@PathVariable String customerId,
                                                   @Valid @RequestBody SubmitKycRequest request) {
        return ApiResponse.ok(customerService.submitKyc(customerId, request));
    }

    @PostMapping("/{customerId}/kyc/review")
    public ApiResponse<CustomerResponse> reviewKyc(@PathVariable String customerId,
                                                   @Valid @RequestBody ReviewKycRequest request) {
        return ApiResponse.ok(customerService.reviewKyc(customerId, request));
    }
}
