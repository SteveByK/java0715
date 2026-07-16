package com.stevebyk.java0715.audit;

import com.stevebyk.java0715.common.ApiResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/audit")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping("/{businessNo}")
    public ApiResponse<List<AuditLogResponse>> byBusinessNo(@PathVariable String businessNo) {
        return ApiResponse.ok(auditService.findByBusinessNo(businessNo));
    }
}
