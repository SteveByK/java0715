package com.stevebyk.java0715.outbox;

import com.stevebyk.java0715.common.ApiResponse;
import com.stevebyk.java0715.common.ddd.InboundAdapter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/outbox")
@Tag(name = "Outbox", description = "Reliable domain event relay APIs")
@InboundAdapter
public class OutboxController {

    private final OutboxService outboxService;

    public OutboxController(OutboxService outboxService) {
        this.outboxService = outboxService;
    }

    @Operation(summary = "Get outbox events", description = "Returns events recorded for one aggregate id.")
    @GetMapping("/{aggregateId}")
    @PreAuthorize("hasAuthority('outbox:read')")
    public ApiResponse<List<OutboxEventResponse>> byAggregateId(@PathVariable String aggregateId) {
        return ApiResponse.ok(outboxService.findByAggregateId(aggregateId));
    }

    @Operation(summary = "Publish pending outbox events", description = "Simulates a reliable relay by marking pending events as published.")
    @PostMapping("/publish-pending")
    @PreAuthorize("hasAuthority('outbox:publish')")
    public ApiResponse<List<OutboxEventResponse>> publishPending() {
        return ApiResponse.ok(outboxService.publishPending());
    }
}
