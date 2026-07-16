package com.stevebyk.java0715.ledger;

import com.stevebyk.java0715.common.ApiResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ledger")
public class LedgerController {

    private final LedgerService ledgerService;

    public LedgerController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @GetMapping("/transactions/{transactionNo}")
    public ApiResponse<List<LedgerEntryResponse>> byTransaction(@PathVariable String transactionNo) {
        return ApiResponse.ok(ledgerService.findByTransactionNo(transactionNo));
    }

    @GetMapping("/accounts/{accountNo}")
    public ApiResponse<List<LedgerEntryResponse>> byAccount(@PathVariable String accountNo) {
        return ApiResponse.ok(ledgerService.findByAccountNo(accountNo));
    }
}
