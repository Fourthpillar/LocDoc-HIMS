-- Removes everything generate.mjs / seed.sql added for the report screens.
-- DEV DATABASE ONLY - never a Flyway migration.
--
-- Seeded rows are identifiable two ways:
--   * an RPT document number: op_visits.op_no 'OP-RPT-%', bills.bill_no 'BILL-RPT-%',
--     patient_registrations.registration_no 'REG-RPT-%', sales_invoices.invoice_number
--     'SI-RPT-%', purchases.grn_number 'GRN-RPT-%' (notes, prescriptions, lines, ratings,
--     payments, discounts, purchase items and ledger entries hang off those)
--   * a 777-microsecond marker on created_date, for rows with no document number of
--     their own (areas, referral doctors, PROs, doctor statuses, schedule exceptions,
--     the seeded weekly sessions, cancellations, audit entries)
-- Anything the app or a person created has neither, so it is left alone.

DELETE FROM stock_ledger_entries WHERE facility_id = 1 AND reference_number LIKE 'SI-RPT-%';
DELETE FROM sales_return_items WHERE sales_return_id IN (SELECT id FROM sales_returns WHERE facility_id = 1 AND return_number LIKE 'SR-RPT-%');
DELETE FROM sales_returns WHERE facility_id = 1 AND return_number LIKE 'SR-RPT-%';
DELETE FROM purchase_items WHERE purchase_id IN (SELECT id FROM purchases WHERE facility_id = 1 AND grn_number LIKE 'GRN-RPT-%');
DELETE FROM purchases WHERE facility_id = 1 AND grn_number LIKE 'GRN-RPT-%';
DELETE FROM purchase_orders WHERE facility_id = 1 AND po_number LIKE 'PO-RPT-%';
DELETE FROM sales_invoice_items WHERE sales_invoice_id IN (SELECT id FROM sales_invoices WHERE facility_id = 1 AND invoice_number LIKE 'SI-RPT-%');
DELETE FROM sales_invoices WHERE facility_id = 1 AND invoice_number LIKE 'SI-RPT-%';
-- Both ends matter: a LIVE visit can be counted as a free review against a SEEDED paid
-- one, and that link blocks the seeded visit's delete on original_op_visit_id.
DELETE FROM free_review_links
WHERE facility_id = 1
  AND (op_visit_id IN (SELECT id FROM op_visits WHERE facility_id = 1 AND op_no LIKE 'OP-RPT-%')
       OR original_op_visit_id IN (SELECT id FROM op_visits WHERE facility_id = 1 AND op_no LIKE 'OP-RPT-%'));
DELETE FROM commission_basis WHERE facility_id = 1 AND EXTRACT(MICROSECOND FROM created_date) = 777;
DELETE FROM consultation_ratings WHERE op_visit_id IN (SELECT id FROM op_visits WHERE facility_id = 1 AND op_no LIKE 'OP-RPT-%');
DELETE FROM prescription_lines WHERE prescription_id IN (SELECT p.id FROM prescriptions p JOIN op_visits v ON v.id = p.op_visit_id WHERE v.facility_id = 1 AND v.op_no LIKE 'OP-RPT-%');
DELETE FROM prescriptions WHERE op_visit_id IN (SELECT id FROM op_visits WHERE facility_id = 1 AND op_no LIKE 'OP-RPT-%');
DELETE FROM consultation_notes WHERE op_visit_id IN (SELECT id FROM op_visits WHERE facility_id = 1 AND op_no LIKE 'OP-RPT-%');
DELETE FROM discounts WHERE bill_id IN (SELECT id FROM bills WHERE facility_id = 1 AND bill_no LIKE 'BILL-RPT-%');
DELETE FROM payments WHERE bill_id IN (SELECT id FROM bills WHERE facility_id = 1 AND bill_no LIKE 'BILL-RPT-%');
DELETE FROM bills WHERE facility_id = 1 AND bill_no LIKE 'BILL-RPT-%';
DELETE FROM op_visits WHERE facility_id = 1 AND op_no LIKE 'OP-RPT-%';
DELETE FROM patient_registrations WHERE facility_id = 1 AND registration_no LIKE 'REG-RPT-%';
DELETE FROM cancellations WHERE facility_id = 1 AND EXTRACT(MICROSECOND FROM created_date) = 777;
DELETE FROM audit_log WHERE facility_id = 1 AND EXTRACT(MICROSECOND FROM occurred_at) = 777;
DELETE FROM doctor_statuses WHERE facility_id = 1 AND EXTRACT(MICROSECOND FROM created_date) = 777;
DELETE FROM schedule_exceptions WHERE facility_id = 1 AND EXTRACT(MICROSECOND FROM created_date) = 777;
DELETE FROM doctor_schedules WHERE facility_id = 1 AND EXTRACT(MICROSECOND FROM created_date) = 777;
UPDATE patients SET area_id = NULL WHERE facility_id = 1 AND area_id IN (SELECT id FROM areas WHERE facility_id = 1 AND EXTRACT(MICROSECOND FROM created_date) = 777);
DELETE FROM areas WHERE facility_id = 1 AND EXTRACT(MICROSECOND FROM created_date) = 777;
DELETE FROM referral_doctors WHERE facility_id = 1 AND EXTRACT(MICROSECOND FROM created_date) = 777;
DELETE FROM pros WHERE facility_id = 1 AND EXTRACT(MICROSECOND FROM created_date) = 777;
