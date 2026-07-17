package com.stevebyk.java0715.account;

import com.stevebyk.java0715.common.ApiResponse;
import com.stevebyk.java0715.common.ddd.InboundAdapter;
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

    @PostMapping
    public ApiResponse<AccountResponse> create(@Valid @RequestBody CreateAccountRequest request) {
        return ApiResponse.ok(accountService.createAccount(request));
    }

    @GetMapping("/{accountNo}")
    public ApiResponse<AccountResponse> get(@PathVariable String accountNo) {
        return ApiResponse.ok(accountService.getAccount(accountNo));
    }

    @PostMapping("/{accountNo}/deposits")
    public ApiResponse<AccountResponse> deposit(@PathVariable String accountNo,
                                                @Valid @RequestBody DepositRequest request) {
        return ApiResponse.ok(accountService.deposit(accountNo, request));
    }

    @PatchMapping("/{accountNo}/status")
    public ApiResponse<AccountResponse> updateStatus(@PathVariable String accountNo,
                                                     @Valid @RequestBody UpdateAccountStatusRequest request) {
        return ApiResponse.ok(accountService.updateStatus(accountNo, request));
    }

    @PostMapping("/{accountNo}/holds")
    public ApiResponse<AccountResponse> holdFunds(@PathVariable String accountNo,
                                                  @Valid @RequestBody HoldFundsRequest request) {
        return ApiResponse.ok(accountService.holdFunds(accountNo, request));
    }

    @PostMapping("/{accountNo}/holds/release")
    public ApiResponse<AccountResponse> releaseFunds(@PathVariable String accountNo,
                                                     @Valid @RequestBody HoldFundsRequest request) {
        return ApiResponse.ok(accountService.releaseFunds(accountNo, request));
    }
}
