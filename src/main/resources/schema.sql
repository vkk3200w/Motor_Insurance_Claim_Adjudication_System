
-- ==========================================
-- 1. POLICY TABLE
-- ==========================================

CREATE TABLE POLICY (
    policy_no VARCHAR(20) PRIMARY KEY,
    product_code VARCHAR(5) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    sum_insured DECIMAL(12,2) NOT NULL,
    deductible DECIMAL(12,2) NOT NULL,
    ncb_percent INT NOT NULL,
    status VARCHAR(10) NOT NULL,
    last_claim_date DATE,
    updated_at TIMESTAMP,
    version INT NOT NULL DEFAULT 0
);

-- ==========================================
-- 2. DEPRECIATION RULE TABLE
-- ==========================================

CREATE TABLE DEPRECIATION_RULE (
    rule_id INT PRIMARY KEY,
    category VARCHAR(10) NOT NULL,
    min_age INT NOT NULL,
    max_age INT NOT NULL,
    dep_percent DECIMAL(5,2) NOT NULL
);

-- ==========================================
-- 3. CLAIM HEADER TABLE
-- ==========================================

CREATE TABLE CLAIM_HEADER (
    claim_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    claim_ref VARCHAR(20) NOT NULL UNIQUE,
    correlation_id VARCHAR(36) NOT NULL,
    source_system VARCHAR(10) NOT NULL,
    policy_no VARCHAR(20) NOT NULL REFERENCES POLICY(policy_no),
    add_ons VARCHAR(100),
    submitted_at_utc TIMESTAMP NOT NULL,
    vehicle_reg_no VARCHAR(12) NOT NULL,
    vehicle_desc VARCHAR(60),
    vehicle_age_years INT NOT NULL,
    claimant_name VARCHAR(80) NOT NULL,
    claimant_mobile_masked CHAR(10) NOT NULL,
    claimant_email VARCHAR(100),
    incident_date DATE NOT NULL,
    incident_time TIME,
    incident_type VARCHAR(10) NOT NULL,
    incident_location VARCHAR(80) NOT NULL,
    fir_number VARCHAR(30),
    garage_code VARCHAR(15) NOT NULL,
    network_garage CHAR(1) NOT NULL,
    total_assessed DECIMAL(12,2) NOT NULL,
    total_depreciation DECIMAL(12,2) NOT NULL,
    deductible_applied DECIMAL(12,2) NOT NULL,
    copay_amount DECIMAL(12,2) NOT NULL,
    payable_amount DECIMAL(12,2) NOT NULL,
    fraud_score INT NOT NULL,
    status VARCHAR(15) NOT NULL,
    status_reason VARCHAR(200),
    payload_hash CHAR(64) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- ==========================================
-- 4. CLAIM LINE TABLE
-- ==========================================

CREATE TABLE CLAIM_LINE (
    claim_line_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    claim_id BIGINT NOT NULL REFERENCES CLAIM_HEADER(claim_id),
    line_no INT NOT NULL,
    part_code VARCHAR(15) NOT NULL,
    category VARCHAR(10) NOT NULL,
    description VARCHAR(100),
    quantity INT NOT NULL,
    unit_price DECIMAL(12,2) NOT NULL,
    tax_amount DECIMAL(12,2) NOT NULL,
    line_total DECIMAL(12,2) NOT NULL,
    dep_percent DECIMAL(5,2) NOT NULL,
    dep_amount DECIMAL(12,2) NOT NULL,
    approved_amount DECIMAL(12,2) NOT NULL,
    line_status VARCHAR(10) NOT NULL,
    exclusion_reason VARCHAR(40),
    CONSTRAINT uq_claim_line UNIQUE (claim_id, line_no)
);

-- ==========================================
-- 5. CLAIM FRAUD FLAG TABLE
-- ==========================================

CREATE TABLE CLAIM_FRAUD_FLAG (
    flag_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    claim_id BIGINT NOT NULL REFERENCES CLAIM_HEADER(claim_id),
    rule_code VARCHAR(25) NOT NULL,
    points INT NOT NULL
);

-- ==========================================
-- 6. CLAIM AUDIT TABLE
-- ==========================================

CREATE TABLE CLAIM_AUDIT (
    audit_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    claim_id BIGINT NOT NULL REFERENCES CLAIM_HEADER(claim_id),
    event_type VARCHAR(15) NOT NULL,
    payload_hash CHAR(64) NOT NULL,
    created_at TIMESTAMP NOT NULL
);
