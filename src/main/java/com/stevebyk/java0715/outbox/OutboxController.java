package com.stevebyk.java0715.outbox;

import com.stevebyk.java0715.common.ApiResponse;
import com.stevebyk.java0715.common.ddd.InboundAdapter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
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

    @GetMapping("/{aggregateId}")
    public ApiResponse<List<OutboxEventResponse>> byAggregateId(@PathVariable String aggregateId) {
        return ApiResponse.ok(outboxService.findByAggregateId(aggregateId));
    }

    @PostMapping("/publish-pending")
    public ApiResponse<List<OutboxEventResponse>> publishPending() {
        return ApiResponse.ok(outboxService.publishPending());
    }
}
