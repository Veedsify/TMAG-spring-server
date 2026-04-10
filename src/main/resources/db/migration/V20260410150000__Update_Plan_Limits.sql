-- Rename column to better reflect the intent (100k employees for Diamond)
ALTER TABLE company_plans
    RENAME COLUMN over_ten_thousand_employees_enabled TO high_employee_limit_enabled;

-- Update plan limits to new values
-- Bronze: 100 employees, 100 credits, no features
UPDATE company_plans SET
    max_employees = 100,
    signup_credits = 100,
    custom_support_enabled = FALSE,
    api_access_enabled = FALSE,
    multiple_admin_accounts_enabled = FALSE,
    high_employee_limit_enabled = FALSE
WHERE code = 'BRONZE';

-- Silver: 500 employees, 200 credits, features enabled, no high limit
UPDATE company_plans SET
    max_employees = 500,
    signup_credits = 200,
    custom_support_enabled = TRUE,
    api_access_enabled = TRUE,
    multiple_admin_accounts_enabled = TRUE,
    high_employee_limit_enabled = FALSE
WHERE code = 'SILVER';

-- Gold: 1000 employees, 500 credits, features enabled, no high limit
UPDATE company_plans SET
    max_employees = 1000,
    signup_credits = 500,
    custom_support_enabled = TRUE,
    api_access_enabled = TRUE,
    multiple_admin_accounts_enabled = TRUE,
    high_employee_limit_enabled = FALSE
WHERE code = 'GOLD';

-- Diamond: 100000 employees, 1000 credits, all features enabled
UPDATE company_plans SET
    max_employees = 100000,
    signup_credits = 1000,
    custom_support_enabled = TRUE,
    api_access_enabled = TRUE,
    multiple_admin_accounts_enabled = TRUE,
    high_employee_limit_enabled = TRUE
WHERE code = 'DIAMOND';
