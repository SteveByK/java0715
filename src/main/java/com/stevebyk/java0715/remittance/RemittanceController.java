package com.stevebyk.java0715.remittance;

import com.stevebyk.java0715.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/remittances")
public class RemittanceController {

    private final RemittanceService remittanceService;

    public RemittanceController(RemittanceService remittanceService) {
        this.remittanceService = remittanceService;
    }

    @PostMapping
    public ApiResponse<RemittanceResponse> remit(@Valid @RequestBody RemittanceRequest request) {
        return ApiResponse.ok(remittanceService.remit(request));
    }
}
