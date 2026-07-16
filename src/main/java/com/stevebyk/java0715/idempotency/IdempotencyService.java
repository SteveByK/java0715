package com.stevebyk.java0715.idempotency;

import com.stevebyk.java0715.common.BusinessException;
import java.time.Instant;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class IdempotencyService {

    private final IdempotencyRepository idempotencyRepository;

    public IdempotencyService(IdempotencyRepository idempotencyRepository) {
        this.idempotencyRepository = idempotencyRepository;
    }

    public void ensureFirstRequest(String requestId, String businessType, String businessNo) {
        if (idempotencyRepository.findByRequestIdAndBusinessType(requestId, businessType).isPresent()) {
            throw new BusinessException("DUPLICATE_REQUEST", "request has already been processed");
        }
        IdempotencyRecord record = new IdempotencyRecord();
        record.setRequestId(requestId);
        record.setBusinessType(businessType);
        record.setBusinessNo(businessNo);
        record.setStatus("SUCCESS");
        record.setCreatedAt(Instant.now());
        record.setUpdatedAt(Instant.now());
        try {
            idempotencyRepository.saveAndFlush(record);
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessException("DUPLICATE_REQUEST", "request has already been processed");
        }
    }

    public Optional<IdempotencyRecord> findExisting(String requestId, String businessType) {
        return idempotencyRepository.findByRequestIdAndBusinessType(requestId, businessType);
    }

    public void markCompleted(String requestId, String businessType, String responseSnapshot) {
        idempotencyRepository.findByRequestIdAndBusinessType(requestId, businessType).ifPresent(record -> {
            record.setStatus("SUCCESS");
            record.setResponseSnapshot(responseSnapshot);
            record.setUpdatedAt(Instant.now());
            idempotencyRepository.save(record);
        });
    }
}
