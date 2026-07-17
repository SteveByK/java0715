CREATE TABLE auth_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id VARCHAR(64) NOT NULL UNIQUE,
    username VARCHAR(64) NOT NULL UNIQUE,
    password_hash VARCHAR(120) NOT NULL,
    display_name VARCHAR(120) NOT NULL,
    email VARCHAR(120),
    phone VARCHAR(32),
    status VARCHAR(32) NOT NULL,
    failed_login_count INT NOT NULL DEFAULT 0,
    locked_until TIMESTAMP NULL,
    last_login_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE auth_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_code VARCHAR(64) NOT NULL UNIQUE,
    role_name VARCHAR(120) NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE auth_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    permission_code VARCHAR(80) NOT NULL UNIQUE,
    permission_name VARCHAR(120) NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE auth_user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_auth_user_role UNIQUE (user_id, role_id),
    CONSTRAINT fk_auth_user_role_user FOREIGN KEY (user_id) REFERENCES auth_user (id),
    CONSTRAINT fk_auth_user_role_role FOREIGN KEY (role_id) REFERENCES auth_role (id)
);

CREATE TABLE auth_role_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_auth_role_permission UNIQUE (role_id, permission_id),
    CONSTRAINT fk_auth_role_permission_role FOREIGN KEY (role_id) REFERENCES auth_role (id),
    CONSTRAINT fk_auth_role_permission_permission FOREIGN KEY (permission_id) REFERENCES auth_permission (id)
);

CREATE TABLE auth_refresh_token (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    token_id VARCHAR(80) NOT NULL UNIQUE,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    user_id VARCHAR(64) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    revoked_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE auth_login_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id VARCHAR(64),
    username VARCHAR(64) NOT NULL,
    result VARCHAR(32) NOT NULL,
    failure_code VARCHAR(64),
    ip_address VARCHAR(64),
    user_agent VARCHAR(256),
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_auth_refresh_token_user ON auth_refresh_token (user_id);
CREATE INDEX idx_auth_login_log_user ON auth_login_log (user_id, created_at);
CREATE INDEX idx_auth_login_log_username ON auth_login_log (username, created_at);

INSERT INTO auth_user (user_id, username, password_hash, display_name, email, phone, status, failed_login_count, created_at, updated_at)
VALUES
('USR_ADMIN_001', 'admin', '$2a$12$vgQeRuFo4EcXy5pRVX.yLOakfERAn4xAGILkEWezgPn6H84bKxuge', 'System Administrator', 'admin@java0715.local', '13800000001', 'ACTIVE', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('USR_TELLER_001', 'teller', '$2a$12$MUH3ncMSGyQXFtq2mQg4b.KFdNWtahJeok2cU1G1nxN9moLQ0ArSu', 'Branch Teller', 'teller@java0715.local', '13800000002', 'ACTIVE', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('USR_AUDITOR_001', 'auditor', '$2a$12$mr/r57z8Frek96VTtfZWfeBUuuk/bzNXN8UOQlAkS063ZFOOvOjsS', 'Risk Auditor', 'auditor@java0715.local', '13800000003', 'ACTIVE', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO auth_role (role_code, role_name, description, created_at)
VALUES
('ADMIN', 'Administrator', 'Full platform operations and configuration access.', CURRENT_TIMESTAMP),
('TELLER', 'Branch Teller', 'Front-office customer, account, transfer and remittance handling.', CURRENT_TIMESTAMP),
('AUDITOR', 'Auditor', 'Read-only audit, ledger and event diagnostics.', CURRENT_TIMESTAMP);

INSERT INTO auth_permission (permission_code, permission_name, description, created_at)
VALUES
('account:create', 'Open account', 'Create domestic and overseas accounts.', CURRENT_TIMESTAMP),
('account:read', 'Read account', 'Read account balance snapshots.', CURRENT_TIMESTAMP),
('account:deposit', 'Deposit funds', 'Credit money into active accounts.', CURRENT_TIMESTAMP),
('account:hold', 'Hold funds', 'Freeze and release account funds.', CURRENT_TIMESTAMP),
('account:status', 'Update account status', 'Freeze, activate or close accounts.', CURRENT_TIMESTAMP),
('transfer:create', 'Create transfer', 'Submit domestic transfer orders.', CURRENT_TIMESTAMP),
('transfer:read', 'Read transfer', 'Read domestic transfer orders.', CURRENT_TIMESTAMP),
('transfer:reverse', 'Reverse transfer', 'Compensate successful domestic transfers.', CURRENT_TIMESTAMP),
('remittance:create', 'Create remittance', 'Submit international remittance orders.', CURRENT_TIMESTAMP),
('remittance:read', 'Read remittance', 'Read international remittance orders.', CURRENT_TIMESTAMP),
('pricing:quote', 'Quote pricing', 'Create remittance quotes from rate and fee rules.', CURRENT_TIMESTAMP),
('ledger:read', 'Read ledger', 'Read append-only ledger entries.', CURRENT_TIMESTAMP),
('audit:read', 'Read audit', 'Read business audit logs.', CURRENT_TIMESTAMP),
('outbox:read', 'Read outbox', 'Read reliable event relay records.', CURRENT_TIMESTAMP),
('outbox:publish', 'Publish outbox', 'Publish pending outbox events.', CURRENT_TIMESTAMP),
('customer:create', 'Create customer', 'Create customer profiles.', CURRENT_TIMESTAMP),
('customer:read', 'Read customer', 'Read customer and KYC profiles.', CURRENT_TIMESTAMP),
('kyc:submit', 'Submit KYC', 'Submit customer KYC records.', CURRENT_TIMESTAMP),
('kyc:review', 'Review KYC', 'Approve or reject customer KYC records.', CURRENT_TIMESTAMP);

INSERT INTO auth_user_role (user_id, role_id, created_at)
SELECT u.id, r.id, CURRENT_TIMESTAMP
  FROM auth_user u
  JOIN auth_role r ON (u.username = 'admin' AND r.role_code = 'ADMIN')
                  OR (u.username = 'teller' AND r.role_code = 'TELLER')
                  OR (u.username = 'auditor' AND r.role_code = 'AUDITOR');

INSERT INTO auth_role_permission (role_id, permission_id, created_at)
SELECT r.id, p.id, CURRENT_TIMESTAMP
  FROM auth_role r
  JOIN auth_permission p ON r.role_code = 'ADMIN';

INSERT INTO auth_role_permission (role_id, permission_id, created_at)
SELECT r.id, p.id, CURRENT_TIMESTAMP
  FROM auth_role r
  JOIN auth_permission p ON p.permission_code IN (
      'account:create', 'account:read', 'account:deposit', 'account:hold',
      'transfer:create', 'transfer:read',
      'remittance:create', 'remittance:read',
      'pricing:quote',
      'customer:create', 'customer:read', 'kyc:submit'
  )
 WHERE r.role_code = 'TELLER';

INSERT INTO auth_role_permission (role_id, permission_id, created_at)
SELECT r.id, p.id, CURRENT_TIMESTAMP
  FROM auth_role r
  JOIN auth_permission p ON p.permission_code IN (
      'account:read', 'transfer:read', 'remittance:read',
      'ledger:read', 'audit:read', 'outbox:read', 'customer:read'
  )
 WHERE r.role_code = 'AUDITOR';
