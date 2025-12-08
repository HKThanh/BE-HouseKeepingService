-- Add email verification fields to Customer and Employee tables
-- Created: 2025-12-08

-- Add is_email_verified column to customer table
ALTER TABLE customer
ADD COLUMN IF NOT EXISTS is_email_verified BOOLEAN DEFAULT false;

-- Add is_email_verified column to employee table
ALTER TABLE employee
ADD COLUMN IF NOT EXISTS is_email_verified BOOLEAN DEFAULT false;

-- Create index for email verification queries
CREATE INDEX IF NOT EXISTS idx_customer_email_verified ON customer(is_email_verified);
CREATE INDEX IF NOT EXISTS idx_employee_email_verified ON employee(is_email_verified);
