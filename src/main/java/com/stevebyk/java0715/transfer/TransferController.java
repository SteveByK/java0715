package com.stevebyk.java0715.transfer;

import com.stevebyk.java0715.common.ApiResponse;
import com.stevebyk.java0715.common.ddd.InboundAdapter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transfers")
@Tag(name = "Domestic Transfer", description = "Domestic transfer orchestration and reversal commands")
@InboundAdapter
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @Operation(summary = "Create domestic transfer", description = "Transfers money between same-currency accounts with risk, locks and ledger entries.")
    @PostMapping("/domestic")
    @PreAuthorize("hasAuthority('transfer:create')")
    public ApiResponse<TransferResponse> domesticTransfer(@Valid @RequestBody TransferRequest request) {
        return ApiResponse.ok(transferService.transfer(request));
    }

    @Operation(summary = "Get transfer order", description = "Returns transfer status, risk result and failure reason if any.")
    @GetMapping("/{orderNo}")
    @PreAuthorize("hasAuthority('transfer:read')")
    public ApiResponse<TransferResponse> get(@PathVariable String orderNo) {
        return ApiResponse.ok(transferService.getByOrderNo(orderNo));
    }

    @Operation(summary = "Reverse transfer", description = "Compensates one successful domestic transfer exactly once.")
    @PostMapping("/{orderNo}/reversals")
    @PreAuthorize("hasAuthority('transfer:reverse')")
    public ApiResponse<ReversalResponse> reverse(@PathVariable String orderNo,
                                                 @Valid @RequestBody ReversalRequest request) {
        return ApiResponse.ok(transferService.reverse(orderNo, request));
    }
}
