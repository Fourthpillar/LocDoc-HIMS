-- ============================================================
-- Version     : V7
-- Description : Enhance the pharmacy schema to support richer
--               reporting, driven by gaps found when comparing
--               against real "AAYURDHARA HOSPITAL" legacy report
--               exports (Suppliers Report, Purchases/GRN report,
--               Sales Report):
--                 1. SUPPLIERS was missing most master-data fields
--                    a real supplier report needs (secondary
--                    contact, structured address, statutory IDs).
--                 2. PURCHASES (GRN) had no payment tracking at
--                    all (no amount_paid/balance_due/due_date),
--                    so a payables/dues-aging report was
--                    impossible - unlike SALES_INVOICES, which
--                    already tracks amount_paid/balance_due.
--                 3. SALES_INVOICES had no round_off_amount, so
--                    the cash-rounding difference between the
--                    computed net amount (with paise) and the
--                    amount actually collected (rounded to the
--                    nearest rupee) was silently absorbed instead
--                    of being tracked - see the balance_due
--                    computation change in SalesService.
-- Author      : LockDoc App
-- ============================================================

-- ---------------------------------------------------------------
-- SUPPLIERS - additional master-data columns
-- ---------------------------------------------------------------
ALTER TABLE suppliers ADD COLUMN IF NOT EXISTS mobile2 VARCHAR(20);
ALTER TABLE suppliers ADD COLUMN IF NOT EXISTS landline VARCHAR(20);
ALTER TABLE suppliers ADD COLUMN IF NOT EXISTS city VARCHAR(100);
ALTER TABLE suppliers ADD COLUMN IF NOT EXISTS state VARCHAR(100);
ALTER TABLE suppliers ADD COLUMN IF NOT EXISTS pincode VARCHAR(10);
ALTER TABLE suppliers ADD COLUMN IF NOT EXISTS tin_no VARCHAR(30);
ALTER TABLE suppliers ADD COLUMN IF NOT EXISTS website VARCHAR(200);
ALTER TABLE suppliers ADD COLUMN IF NOT EXISTS supplier_type VARCHAR(30);
ALTER TABLE suppliers ADD COLUMN IF NOT EXISTS drug_license_no VARCHAR(50);

-- ---------------------------------------------------------------
-- PURCHASES (GRN) - payment/dues tracking, mirroring the
-- amount_paid/balance_due pattern already used on sales_invoices
-- ---------------------------------------------------------------
ALTER TABLE purchases ADD COLUMN IF NOT EXISTS amount_paid DECIMAL(12,2) NOT NULL DEFAULT 0;
ALTER TABLE purchases ADD COLUMN IF NOT EXISTS balance_due DECIMAL(12,2) NOT NULL DEFAULT 0;
ALTER TABLE purchases ADD COLUMN IF NOT EXISTS due_date DATE;

-- Backfill: existing GRNs have amount_paid = 0, so their full
-- total_amount is outstanding until paid.
UPDATE purchases SET balance_due = total_amount - amount_paid;

-- ---------------------------------------------------------------
-- SALES_INVOICES - explicit round-off tracking
-- ---------------------------------------------------------------
ALTER TABLE sales_invoices ADD COLUMN IF NOT EXISTS round_off_amount DECIMAL(10,2) NOT NULL DEFAULT 0;
