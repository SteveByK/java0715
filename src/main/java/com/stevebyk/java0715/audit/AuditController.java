package com.stevebyk.java0715.audit;

import com.stevebyk.java0715.common.ApiResponse;
import com.stevebyk.java0715.common.ddd.InboundAdapter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/audit")
@Tag(name = "Audit", description = "Business audit trail query APIs")
@InboundAdapter
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @Operation(summary = "Get audit logs", description = "Returns business audit events for one business number.")
    @GetMapping("/{businessNo}")
    public ApiResponse<List<AuditLogResponse>> byBusinessNo(@PathVariable String businessNo) {
        return ApiResponse.ok(auditService.findByBusinessNo(businessNo));
    }
}
