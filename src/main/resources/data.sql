
-- ==========================================
-- 1. POLICY SEED DATA
-- ==========================================

INSERT INTO POLICY (
    policy_no, product_code, start_date, end_date,
    sum_insured, deductible, ncb_percent, status,
    last_claim_date, updated_at, version
) VALUES
(
    'MTR-TN-778812', 'COMP',
    '2026-01-01', '2026-12-31',
    650000.00, 2000.00, 25, 'ACTIVE',
    NULL, NULL, 0
),
(
    'MTR-TN-778813', 'TP',
    '2026-01-01', '2026-12-31',
    0.00, 0.00, 0, 'ACTIVE',
    NULL, NULL, 0
),
(
    'MTR-KA-100201', 'COMP',
    '2026-09-20', '2027-09-19',
    420000.00, 1000.00, 0, 'ACTIVE',
    NULL, NULL, 0
),
(
    'MTR-MH-300400', 'COMP',
    '2025-01-01', '2025-12-31',
    900000.00, 2500.00, 35, 'LAPSED',
    NULL, NULL, 0
);

-- ==========================================
-- 2. DEPRECIATION RULE SEED DATA
-- ==========================================

-- METAL: Age-based depreciation

INSERT INTO DEPRECIATION_RULE
(rule_id, category, min_age, max_age, dep_percent)
VALUES
(1, 'METAL', 0, 0, 0.00),
(2, 'METAL', 1, 1, 5.00),
(3, 'METAL', 2, 2, 10.00),
(4, 'METAL', 3, 3, 15.00),
(5, 'METAL', 4, 4, 25.00),
(6, 'METAL', 5, 99, 35.00);

-- PLASTIC: Fixed 50% depreciation

INSERT INTO DEPRECIATION_RULE
(rule_id, category, min_age, max_age, dep_percent)
VALUES
(7, 'PLASTIC', 0, 99, 50.00);

-- RUBBER: Fixed 50% depreciation

INSERT INTO DEPRECIATION_RULE
(rule_id, category, min_age, max_age, dep_percent)
VALUES
(8, 'RUBBER', 0, 99, 50.00);

-- GLASS: No depreciation

INSERT INTO DEPRECIATION_RULE
(rule_id, category, min_age, max_age, dep_percent)
VALUES
(9, 'GLASS', 0, 99, 0.00);

-- PAINT: Fixed 25% depreciation

INSERT INTO DEPRECIATION_RULE
(rule_id, category, min_age, max_age, dep_percent)
VALUES
(10, 'PAINT', 0, 99, 25.00);

-- LABOUR: No depreciation

INSERT INTO DEPRECIATION_RULE
(rule_id, category, min_age, max_age, dep_percent)
VALUES
(11, 'LABOUR', 0, 99, 0.00);
