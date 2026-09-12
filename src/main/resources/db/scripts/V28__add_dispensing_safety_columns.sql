-- ============================================================
-- Version     : V28
-- Description : Step 6 - dispensing-side safety checks (Master Spec
--               §11.5/§11.6), the other half of the doctor-to-pharmacy
--               handoff this step is named for.
--
--               medicines: drug_schedule (OTC/H/H1/X/NARCOTIC) drives
--               mandatory prescriber capture below - kept alongside the
--               existing is_schedule_drug boolean rather than replacing
--               it, since that flag is already wired into
--               MedicineService/DTOs and this is strictly additive.
--               high_alert drives the confirmation-step requirement.
--
--               sales_invoice_items: prescription_line_id is a soft
--               reference (no FK - Prescription lives in the Doctor
--               Module's own bounded context, matching the existing
--               "intentionally not a JPA relation" pattern already used
--               for medicine_batches.source_purchase_item_id).
--               override_reason records a soft-stop override (allergy/
--               high-alert/duplicate-therapy) - "never silent" per
--               §11.5. prescriber_name/prescriber_registration_number
--               are mandatory (enforced in SalesService, not the schema)
--               only when the line's medicine is a scheduled drug.
-- Author      : LockDoc App
-- ============================================================

ALTER TABLE medicines ADD COLUMN IF NOT EXISTS drug_schedule VARCHAR(10);
ALTER TABLE medicines ADD COLUMN IF NOT EXISTS high_alert BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE sales_invoice_items ADD COLUMN IF NOT EXISTS prescription_line_id BIGINT;
ALTER TABLE sales_invoice_items ADD COLUMN IF NOT EXISTS override_reason VARCHAR(300);
ALTER TABLE sales_invoice_items ADD COLUMN IF NOT EXISTS prescriber_name VARCHAR(150);
ALTER TABLE sales_invoice_items ADD COLUMN IF NOT EXISTS prescriber_registration_number VARCHAR(100);
