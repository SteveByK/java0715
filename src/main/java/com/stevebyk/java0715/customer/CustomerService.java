package com.stevebyk.java0715.customer;

import com.stevebyk.java0715.audit.AuditService;
import com.stevebyk.java0715.common.BusinessException;
import com.stevebyk.java0715.common.ddd.ApplicationServiceRole;
import com.stevebyk.java0715.outbox.OutboxService;
import java.time.Instant;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service for customer profile and KYC lifecycle.
 *
 * <p>Customer and KYC state is kept separate from payment order state so payment
 * contexts can depend on stable customer identifiers without owning onboarding
 * workflows.</p>
 */
@Service
@ApplicationServiceRole
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final KycRepository kycRepository;
    private final AuditService auditService;
    private final OutboxService outboxService;

    public CustomerService(CustomerRepository customerRepository, KycRepository kycRepository,
                           AuditService auditService, OutboxService outboxService) {
        this.customerRepository = customerRepository;
        this.kycRepository = kycRepository;
        this.auditService = auditService;
        this.outboxService = outboxService;
    }

    @Transactional
    public CustomerResponse createCustomer(CreateCustomerRequest request) {
        CustomerEntity customer = new CustomerEntity();
        customer.setCustomerId(request.customerId());
        customer.setFullName(request.fullName());
        customer.setUserRegion(request.userRegion());
        customer.setCountryCode(request.countryCode().toUpperCase());
        customer.setPhone(request.phone());
        customer.setEmail(request.email());
        customer.setRiskLevel(RiskLevel.LOW);
        customer.setStatus(CustomerStatus.ACTIVE);
        customer.setCreatedAt(Instant.now());
        customer.setUpdatedAt(Instant.now());
        try {
            CustomerEntity saved = customerRepository.saveAndFlush(customer);
            auditService.record(saved.getCustomerId(), "CREATE_CUSTOMER", "SUCCESS", "customer registered");
            outboxService.publish(saved.getCustomerId(), "CustomerCreatedEvent",
                    "{\"customerId\":\"" + saved.getCustomerId() + "\"}");
            return CustomerResponse.from(saved, null);
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessException("CUSTOMER_DUPLICATED", "customer id already exists");
        }
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomer(String customerId) {
        CustomerEntity customer = loadCustomer(customerId);
        KycEntity kyc = kycRepository.findByCustomerId(customerId).orElse(null);
        return CustomerResponse.from(customer, kyc);
    }

    @Transactional
    public CustomerResponse submitKyc(String customerId, SubmitKycRequest request) {
        CustomerEntity customer = loadCustomer(customerId);
        KycEntity kyc = kycRepository.findByCustomerId(customerId).orElseGet(KycEntity::new);
        kyc.setCustomerId(customerId);
        kyc.setDocumentType(request.documentType());
        kyc.setMaskedDocumentNo(request.maskedDocumentNo());
        kyc.setKycLevel(request.kycLevel());
        kyc.setStatus(KycStatus.PENDING);
        kyc.setCreatedAt(kyc.getCreatedAt() == null ? Instant.now() : kyc.getCreatedAt());
        kyc.setUpdatedAt(Instant.now());
        KycEntity savedKyc = kycRepository.save(kyc);
        auditService.record(customerId, "SUBMIT_KYC", "SUCCESS", "kyc submitted");
        outboxService.publish(customerId, "KycSubmittedEvent", "{\"customerId\":\"" + customerId + "\"}");
        return CustomerResponse.from(customer, savedKyc);
    }

    @Transactional
    public CustomerResponse reviewKyc(String customerId, ReviewKycRequest request) {
        CustomerEntity customer = loadCustomer(customerId);
        KycEntity kyc = kycRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new BusinessException("KYC_NOT_FOUND", "kyc profile not found"));
        kyc.setStatus(request.status());
        kyc.setReviewedBy(request.reviewedBy());
        kyc.setReviewedAt(Instant.now());
        kyc.setUpdatedAt(Instant.now());
        auditService.record(customerId, "REVIEW_KYC", request.status().name(), "kyc reviewed");
        outboxService.publish(customerId, "KycReviewedEvent",
                "{\"customerId\":\"" + customerId + "\",\"status\":\"" + request.status() + "\"}");
        return CustomerResponse.from(customer, kyc);
    }

    private CustomerEntity loadCustomer(String customerId) {
        return customerRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new BusinessException("CUSTOMER_NOT_FOUND", "customer not found"));
    }
}
