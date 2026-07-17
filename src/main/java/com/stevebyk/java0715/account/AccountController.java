package com.stevebyk.java0715.account;

import com.stevebyk.java0715.common.ApiResponse;
import com.stevebyk.java0715.common.ddd.InboundAdapter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/accounts")
@Tag(name = "Account", description = "Account aggregate commands and balance queries")
@InboundAdapter
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @Operation(summary = "Open account", description = "Creates a domestic or overseas account aggregate.")
    @PostMapping
    public ApiResponse<AccountResponse> create(@Valid @RequestBody CreateAccountRequest request) {
        return ApiResponse.ok(accountService.createAccount(request));
    }

    @Operation(summary = "Get account", description = "Returns the current balance snapshot and account status.")
    @GetMapping("/{accountNo}")
    public ApiResponse<AccountResponse> get(@PathVariable String accountNo) {
        return ApiResponse.ok(accountService.getAccount(accountNo));
    }

    @Operation(summary = "Deposit funds", description = "Credits an active account and writes ledger, audit and outbox records.")
    @PostMapping("/{accountNo}/deposits")
    public ApiResponse<AccountResponse> deposit(@PathVariable String accountNo,
                                                @Valid @RequestBody DepositRequest request) {
        return ApiResponse.ok(accountService.deposit(accountNo, request));
    }

    @Operation(summary = "Update account status", description = "Freezes, activates or closes an account after an operational decision.")
    @PatchMapping("/{accountNo}/status")
    public ApiResponse<AccountResponse> updateStatus(@PathVariable String accountNo,
                                                     @Valid @RequestBody UpdateAccountStatusRequest request) {
        return ApiResponse.ok(accountService.updateStatus(accountNo, request));
    }

    @Operation(summary = "Hold funds", description = "Moves available balance into frozen balance for authorization or risk hold.")
    @PostMapping("/{accountNo}/holds")
    public ApiResponse<AccountResponse> holdFunds(@PathVariable String accountNo,
                                                  @Valid @RequestBody HoldFundsRequest request) {
        return ApiResponse.ok(accountService.holdFunds(accountNo, request));
    }

    @Operation(summary = "Release held funds", description = "Moves part of frozen balance back into available balance.")
    @PostMapping("/{accountNo}/holds/release")
    public ApiResponse<AccountResponse> releaseFunds(@PathVariable String accountNo,
                                                     @Valid @RequestBody HoldFundsRequest request) {
        return ApiResponse.ok(accountService.releaseFunds(accountNo, request));
    }
}
