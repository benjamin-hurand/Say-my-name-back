-- Migration: Fix reserved SQL keywords in schema
-- Date: 2026-01-10
-- Issue: SQL reserved keywords ("key", "value") cause errors in H2 and fragile in MySQL
-- Solution:
--   1. Rename organizations.key → org_key
--   2. Rename person_attributes.value → attribute_value

-- ============================================================================
-- PART A: Fix organizations.key → org_key
-- ============================================================================

-- STEP A1: Check if migration is needed
-- Run this query first to verify current state:
-- SHOW COLUMNS FROM organizations WHERE Field = 'key';
-- If result is empty, migration already applied or column is 'org_key'

-- STEP A2: Rename column (MySQL syntax)
-- Note: ALTER TABLE CHANGE preserves data and indexes in MySQL
ALTER TABLE organizations
    CHANGE COLUMN `key` org_key VARCHAR(64) NOT NULL;

-- STEP A3: Drop old constraint (if exists)
-- MySQL allows IF EXISTS since 5.7.11
ALTER TABLE organizations
    DROP INDEX IF EXISTS uk_organizations_key;

ALTER TABLE organizations
    DROP INDEX IF EXISTS idx_organizations_key;

-- STEP A4: Create new constraints with correct column name
-- Unique constraint on org_key
ALTER TABLE organizations
    ADD CONSTRAINT uk_organizations_org_key UNIQUE (org_key);

-- Index for performance (if not auto-created by unique constraint)
CREATE INDEX idx_organizations_org_key ON organizations(org_key);

-- ============================================================================
-- PART B: Fix person_attributes.value → attribute_value
-- ============================================================================

-- STEP B1: Check if migration is needed
-- SHOW COLUMNS FROM person_attributes WHERE Field = 'value';

-- STEP B2: Rename column (MySQL syntax)
ALTER TABLE person_attributes
    CHANGE COLUMN `value` attribute_value VARCHAR(255) NULL;

-- ============================================================================
-- VERIFICATION
-- ============================================================================
-- Run these queries to verify:
--
-- 1. Check organizations.org_key:
-- SHOW COLUMNS FROM organizations WHERE Field = 'org_key';
-- SHOW INDEXES FROM organizations WHERE Column_name = 'org_key';
--
-- 2. Check person_attributes.attribute_value:
-- SHOW COLUMNS FROM person_attributes WHERE Field = 'attribute_value';
--
-- Expected results:
-- - organizations.org_key: Type varchar(64), Null NO, Index UNIQUE
-- - person_attributes.attribute_value: Type varchar(255), Null YES

-- ============================================================================
-- ROLLBACK (if needed)
-- ============================================================================
-- WARNING: Only use if migration failed and you need to revert
--
-- -- Rollback Part B: person_attributes
-- ALTER TABLE person_attributes
--     CHANGE COLUMN attribute_value `value` VARCHAR(255) NULL;
--
-- -- Rollback Part A: organizations
-- ALTER TABLE organizations
--     CHANGE COLUMN org_key `key` VARCHAR(64) NOT NULL;
--
-- ALTER TABLE organizations
--     DROP INDEX IF EXISTS uk_organizations_org_key;
--
-- ALTER TABLE organizations
--     DROP INDEX IF EXISTS idx_organizations_org_key;
--
-- ALTER TABLE organizations
--     ADD CONSTRAINT uk_organizations_key UNIQUE (`key`);
--
-- CREATE INDEX idx_organizations_key ON organizations(`key`);
