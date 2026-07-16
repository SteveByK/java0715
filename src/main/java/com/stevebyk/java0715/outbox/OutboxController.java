package com.stevebyk.java0715.outbox;

import com.stevebyk.java0715.common.ApiResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/outbox")
public class OutboxController {

    private final OutboxService outboxService;

    public OutboxController(OutboxService outboxService) {
        this.outboxService = outboxService;
    }

    @GetMapping("/{aggregateId}")
    public ApiResponse<List<OutboxEventResponse>> byAggregateId(@PathVariable String aggregateId) {
        return ApiResponse.ok(outboxService.findByAggregateId(aggregateId));
    }
}
