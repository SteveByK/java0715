insert into account_balance (
    account_no, customer_id, owner_name, user_region, account_type, currency,
    available_balance, frozen_balance, status, version, created_at, updated_at
) values
('AC_DEMO_CNY_001', 'C_DEMO_001', 'Zhang San Demo', 'DOMESTIC', 'SAVINGS', 'CNY', 97797.9000, 0.0000, 'ACTIVE', 0, current_timestamp, current_timestamp),
('AC_DEMO_CNY_002', 'C_DEMO_002', 'Li Si Demo', 'DOMESTIC', 'SAVINGS', 'CNY', 1500.0000, 0.0000, 'ACTIVE', 0, current_timestamp, current_timestamp),
('AC_DEMO_USD_001', 'U_DEMO_001', 'Alice Smith Demo', 'OVERSEAS', 'FOREIGN_CURRENCY', 'USD', 98.0000, 0.0000, 'ACTIVE', 0, current_timestamp, current_timestamp),
('AC_DEMO_USD_002', 'U_DEMO_002', 'Bob Wilson Demo', 'OVERSEAS', 'CHECKING', 'USD', 12000.0000, 0.0000, 'ACTIVE', 0, current_timestamp, current_timestamp),
('AC_DEMO_CNY_HIGH', 'C_DEMO_003', 'High Balance Demo', 'DOMESTIC', 'SAVINGS', 'CNY', 300000.0000, 0.0000, 'ACTIVE', 0, current_timestamp, current_timestamp),
('AC_DEMO_CNY_FROZEN', 'C_DEMO_004', 'Frozen Demo', 'DOMESTIC', 'SAVINGS', 'CNY', 5000.0000, 5000.0000, 'FROZEN', 0, current_timestamp, current_timestamp);

insert into ledger_entry (
    entry_no, transaction_no, account_no, direction, amount, balance_after, currency, entry_type, created_at
) values
('LE_DEMO_DEP_001', 'DEMO_DEP_001', 'AC_DEMO_CNY_001', 'CREDIT', 100000.0000, 100000.0000, 'CNY', 'DEMO_SEED_DEPOSIT', current_timestamp),
('LE_DEMO_TR_001_D', 'TR_DEMO_SUCCESS', 'AC_DEMO_CNY_001', 'DEBIT', 1500.0000, 98500.0000, 'CNY', 'DOMESTIC_TRANSFER', current_timestamp),
('LE_DEMO_TR_001_C', 'TR_DEMO_SUCCESS', 'AC_DEMO_CNY_002', 'CREDIT', 1500.0000, 1500.0000, 'CNY', 'DOMESTIC_TRANSFER', current_timestamp),
('LE_DEMO_RM_001_D', 'RM_DEMO_SUCCESS', 'AC_DEMO_CNY_001', 'DEBIT', 702.1000, 97797.9000, 'CNY', 'REMITTANCE_DEBIT', current_timestamp),
('LE_DEMO_RM_001_C', 'RM_DEMO_SUCCESS', 'AC_DEMO_USD_001', 'CREDIT', 98.0000, 98.0000, 'USD', 'REMITTANCE_CREDIT', current_timestamp);

insert into transfer_order (
    order_no, request_id, from_account_no, to_account_no, amount, fee, currency,
    status, risk_code, failure_reason, created_at, updated_at
) values
('TR_DEMO_SUCCESS', 'demo-tr-success', 'AC_DEMO_CNY_001', 'AC_DEMO_CNY_002', 1500.0000, 0.0000, 'CNY', 'SUCCESS', null, null, current_timestamp, current_timestamp),
('TR_DEMO_RISK_REJECTED', 'demo-tr-risk', 'AC_DEMO_CNY_HIGH', 'AC_DEMO_CNY_002', 250000.0000, 0.0000, 'CNY', 'RISK_REJECTED', 'DOMESTIC_HIGH_AMOUNT', 'domestic transfer amount requires manual review', current_timestamp, current_timestamp);

insert into remittance_order (
    order_no, request_id, sender_account_no, receiver_account_no, source_amount,
    exchange_rate, fee, target_amount, source_currency, target_currency, destination_country,
    swift_code, iban, status, risk_code, failure_reason, created_at, updated_at
) values
('RM_DEMO_SUCCESS', 'demo-rm-success', 'AC_DEMO_CNY_001', 'AC_DEMO_USD_001', 700.0000, 0.14000000, 2.1000, 98.0000, 'CNY', 'USD', 'US', 'BOFAUS3N', null, 'SUCCESS', null, null, current_timestamp, current_timestamp),
('RM_DEMO_RISK_REJECTED', 'demo-rm-risk', 'AC_DEMO_USD_002', 'AC_DEMO_CNY_002', 60000.0000, 7.20000000, 180.0000, 432000.0000, 'USD', 'CNY', 'IR', 'DEMOIRXX', null, 'RISK_REJECTED', 'HIGH_RISK_COUNTRY', 'destination country is blocked by risk policy', current_timestamp, current_timestamp);

insert into idempotency_record (
    request_id, business_type, business_no, status, created_at
) values
('demo-dep-success', 'DEPOSIT', 'DEMO_DEP_001', 'SUCCESS', current_timestamp),
('demo-tr-success', 'DOMESTIC_TRANSFER', 'TR_DEMO_SUCCESS', 'SUCCESS', current_timestamp),
('demo-tr-risk', 'DOMESTIC_TRANSFER', 'TR_DEMO_RISK_REJECTED', 'SUCCESS', current_timestamp),
('demo-rm-success', 'INTERNATIONAL_REMITTANCE', 'RM_DEMO_SUCCESS', 'SUCCESS', current_timestamp),
('demo-rm-risk', 'INTERNATIONAL_REMITTANCE', 'RM_DEMO_RISK_REJECTED', 'SUCCESS', current_timestamp);

insert into audit_log (
    business_no, action, result, detail, created_at
) values
('AC_DEMO_CNY_001', 'CREATE_ACCOUNT', 'SUCCESS', 'seed domestic source account', current_timestamp),
('AC_DEMO_CNY_002', 'CREATE_ACCOUNT', 'SUCCESS', 'seed domestic receiver account', current_timestamp),
('AC_DEMO_USD_001', 'CREATE_ACCOUNT', 'SUCCESS', 'seed overseas receiver account', current_timestamp),
('DEMO_DEP_001', 'DEPOSIT', 'SUCCESS', 'seed deposit scenario', current_timestamp),
('TR_DEMO_SUCCESS', 'DOMESTIC_TRANSFER', 'SUCCESS', 'seed domestic transfer scenario', current_timestamp),
('TR_DEMO_RISK_REJECTED', 'DOMESTIC_TRANSFER', 'RISK_REJECTED', 'seed high amount transfer rejection', current_timestamp),
('RM_DEMO_SUCCESS', 'INTERNATIONAL_REMITTANCE', 'SUCCESS', 'seed international remittance scenario', current_timestamp),
('RM_DEMO_RISK_REJECTED', 'INTERNATIONAL_REMITTANCE', 'RISK_REJECTED', 'seed blocked country remittance rejection', current_timestamp),
('AC_DEMO_CNY_FROZEN', 'UPDATE_ACCOUNT_STATUS', 'SUCCESS', 'seed frozen account scenario', current_timestamp);

insert into outbox_event (
    event_id, aggregate_id, event_type, payload, status, created_at
) values
('EVT_DEMO_ACCOUNT_001', 'AC_DEMO_CNY_001', 'AccountOpenedEvent', '{"accountNo":"AC_DEMO_CNY_001"}', 'NEW', current_timestamp),
('EVT_DEMO_DEPOSIT_001', 'DEMO_DEP_001', 'DepositCompletedEvent', '{"accountNo":"AC_DEMO_CNY_001"}', 'NEW', current_timestamp),
('EVT_DEMO_TRANSFER_001', 'TR_DEMO_SUCCESS', 'TransferSucceededEvent', '{"orderNo":"TR_DEMO_SUCCESS"}', 'NEW', current_timestamp),
('EVT_DEMO_REMIT_001', 'RM_DEMO_SUCCESS', 'RemittanceCompletedEvent', '{"orderNo":"RM_DEMO_SUCCESS"}', 'NEW', current_timestamp),
('EVT_DEMO_STATUS_001', 'AC_DEMO_CNY_FROZEN', 'AccountStatusChangedEvent', '{"accountNo":"AC_DEMO_CNY_FROZEN","status":"FROZEN"}', 'NEW', current_timestamp);
