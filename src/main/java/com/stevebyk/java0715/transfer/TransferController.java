package com.stevebyk.java0715.transfer;

import com.stevebyk.java0715.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transfers")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping("/domestic")
    public ApiResponse<TransferResponse> domesticTransfer(@Valid @RequestBody TransferRequest request) {
        return ApiResponse.ok(transferService.transfer(request));
    }

    @GetMapping("/{orderNo}")
    public ApiResponse<TransferResponse> get(@PathVariable String orderNo) {
        return ApiResponse.ok(transferService.getByOrderNo(orderNo));
    }
}
