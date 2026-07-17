package com.stevebyk.java0715.pricing;

import com.stevebyk.java0715.common.ApiResponse;
import com.stevebyk.java0715.common.ddd.InboundAdapter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/pricing")
@Tag(name = "Pricing", description = "Database-backed exchange-rate and fee quote APIs")
@InboundAdapter
public class PricingController {

    private final PricingService pricingService;

    public PricingController(PricingService pricingService) {
        this.pricingService = pricingService;
    }

    @GetMapping("/remittance-quote")
    public ApiResponse<QuoteResponse> quoteRemittance(
            @RequestParam @NotBlank String sourceCurrency,
            @RequestParam @NotBlank String targetCurrency,
            @RequestParam @DecimalMin("0.01") BigDecimal sourceAmount) {
        return ApiResponse.ok(pricingService.quoteRemittance(sourceCurrency, targetCurrency, sourceAmount));
    }
}
