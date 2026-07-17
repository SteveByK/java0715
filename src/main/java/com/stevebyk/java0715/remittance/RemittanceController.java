package com.stevebyk.java0715.remittance;

import com.stevebyk.java0715.common.ApiResponse;
import com.stevebyk.java0715.common.ddd.InboundAdapter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/remittances")
@Tag(name = "International Remittance", description = "Cross-border remittance orders with quote locking")
@InboundAdapter
public class RemittanceController {

    private final RemittanceService remittanceService;

    public RemittanceController(RemittanceService remittanceService) {
        this.remittanceService = remittanceService;
    }

    @Operation(summary = "Create remittance", description = "Consumes a locked quote and settles cross-currency remittance.")
    @PostMapping
    public ApiResponse<RemittanceResponse> remit(@Valid @RequestBody RemittanceRequest request) {
        return ApiResponse.ok(remittanceService.remit(request));
    }

    @Operation(summary = "Get remittance order", description = "Returns remittance status, pricing trace and risk result.")
    @GetMapping("/{orderNo}")
    public ApiResponse<RemittanceResponse> get(@PathVariable String orderNo) {
        return ApiResponse.ok(remittanceService.getByOrderNo(orderNo));
    }
}
