-- Enforce valid tenant role codes at the database level

ALTER TABLE tenant_users
ADD CONSTRAINT chk_tenant_users_role_code
CHECK (role_code IN ('OWNER', 'ADMIN', 'MEMBER'));
