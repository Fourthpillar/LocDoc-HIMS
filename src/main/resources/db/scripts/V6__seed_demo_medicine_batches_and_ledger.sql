-- ============================================================
-- Version     : V6
-- Description : Seed real demo medicine batch and stock ledger
--               data transcribed from a genuine "Medicine Stock
--               Report" issued by AAYURDHARA HOSPITAL, dated
--               06/09/2026. One medicine_batches row per source
--               report line (249 rows total - a medicine name
--               appearing multiple times in the source becomes
--               multiple batches of the same medicine). Each
--               batch gets a matching stock_ledger_entries row
--               representing its opening balance.
--
--               This is a pure opening-balance load with no
--               backing purchase document, so:
--                 - medicine_batches.source_purchase_item_id is
--                   NULL for every row (there is no purchase_items
--                   row behind it)
--                 - stock_ledger_entries.txn_type and
--                   .reference_type both use a new literal value,
--                   'OPENING_STOCK' (txn_type/reference_type are
--                   plain VARCHAR columns with no Java-side enum,
--                   so this required no code change - see
--                   StockLedgerEntry.java)
--                 - stock_ledger_entries.reference_id
--                   self-references the medicine_batches.id it
--                   belongs to (there is no separate document row
--                   to point at)
--                 - reference_number is the fixed literal
--                   'OPENING-STOCK-2026-09-06' and txn_date is
--                   2026-09-06 00:00:00, matching the source
--                   report's "as on date"
--
--               sale_rate has no source column, so it is defaulted
--               to the same value as mrp.
-- Author      : LockDoc App
-- ============================================================

-- ---------------------------------------------------------------
-- MEDICINE_BATCHES (249 rows - one per source report line)
-- ---------------------------------------------------------------
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '0726A021', DATE '2027-12-31', 3.57, 2.55, 3.57, 82, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0001' AND s.name = 'GANESH MEDICAL STORES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '072C024', DATE '2028-02-29', 6.35, 4.57, 6.35, 143, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0002' AND s.name = 'GANESH MEDICAL STORES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '1245428', DATE '2027-08-31', 42.53, 31.36, 42.53, 28, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0003' AND s.name = 'SHRAVANI ENTERPRISES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'E51437', DATE '2028-10-31', 25.13, 15.08, 25.13, 70, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0004' AND s.name = 'SERONIA LIFESCIENCES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'K2AIY069', DATE '2027-07-31', 130.00, 75.00, 130.00, 7, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0005' AND s.name = 'KRISHNAM MEDICAL AGENCY';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'G55Y024', DATE '2028-04-30', 1.68, 1.28, 1.68, 425, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0006' AND s.name = 'SERONIA LIFESCIENCES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'A1AJX175', DATE '2026-10-31', 59.00, 32.45, 59.00, 11, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0007' AND s.name = 'SERONIA LIFESCIENCES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '11241612', DATE '2026-11-30', 154.50, 112.48, 154.50, 1, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0008' AND s.name = 'KRISHNAM MEDICAL AGENCY';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '05251631A', DATE '2027-05-31', 121.87, 94.75, 121.87, 3, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0009' AND s.name = 'SERONIA LIFESCIENCES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '11251682', DATE '2027-11-30', 144.85, 96.57, 144.85, 11, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0010' AND s.name = 'KRISHNAM MEDICAL AGENCY';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'U2AIY025', DATE '2027-09-30', 75.48, 56.16, 75.48, 2, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0011' AND s.name = 'SERONIA LIFESCIENCES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'E2402669', DATE '2026-10-31', 3.85, 2.77, 3.85, 115, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0012' AND s.name = 'SERONIA LIFESCIENCES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'E2600053', DATE '2027-12-31', 7.09, 5.11, 7.09, 65, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0013' AND s.name = 'GANESH MEDICAL STORES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'AATI25004', DATE '2028-05-31', 23.55, 16.43, 23.55, 70, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0014' AND s.name = 'GANESH MEDICAL STORES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'IA00084A', DATE '2028-03-31', 5.66, 3.92, 5.66, 80, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0015' AND s.name = 'PAVAN AGENCIES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '826D182', DATE '2027-10-31', 19.67, 13.49, 19.67, 22, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0016' AND s.name = 'KRISHNAM MEDICAL AGENCY';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '6SN0153', DATE '2028-10-31', 25.18, 16.11, 25.18, 9, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0017' AND s.name = 'KRISHNAM MEDICAL AGENCY';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '2408001433', DATE '2026-10-31', 13.04, 10.44, 13.04, 72, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0018' AND s.name = 'SHIV MEDICAL AGENCY';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '2508001017', DATE '2027-07-31', 26.86, 18.13, 26.86, 1, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0019' AND s.name = 'PAVAN AGENCIES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'XXXX', DATE '2027-12-31', 100.00, 28.00, 100.00, 5, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0020' AND s.name = 'SRK SURGICALS';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'XXXXX', DATE '2028-12-31', 150.00, 39.20, 150.00, 4, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0021' AND s.name = 'SRK SURGICALS';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'A6AGX036', DATE '2027-05-31', 9.09, 6.35, 9.09, 7, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0022' AND s.name = 'SERONIA LIFESCIENCES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'GF00126', DATE '2028-05-31', 132.30, 97.24, 132.30, 4, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0023' AND s.name = 'SERONIA LIFESCIENCES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'EY26869004', DATE '2028-03-31', 13.33, 8.89, 13.33, 45, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0024' AND s.name = 'KRISHNAM MEDICAL AGENCY';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'AXC0179', DATE '2027-10-31', 21.28, 15.32, 21.28, 10, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0025' AND s.name = 'KRISHNAM MEDICAL AGENCY';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'RHL41', DATE '2027-01-31', 14.79, 10.50, 14.79, 44, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0026' AND s.name = 'SHRAVANI ENTERPRISES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'TT25031', DATE '2027-08-31', 35.07, 10.52, 35.07, 44, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0027' AND s.name = 'SERONIA LIFESCIENCES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '50230066', DATE '2027-08-31', 11.21, 8.34, 11.21, 24, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0028' AND s.name = 'SERONIA LIFESCIENCES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'AIW02CHA', DATE '2027-09-30', 7.63, 5.09, 7.63, 195, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0029' AND s.name = 'VASU AGENCIES DRUGS PRIVATE LIMITED';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'CTH26002', DATE '2028-04-30', 96.56, 64.37, 96.56, 176, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0030' AND s.name = 'VASU AGENCIES DRUGS PRIVATE LIMITED';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'A25045ZP', DATE '2027-01-31', 56.66, 42.49, 56.66, 44, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0031' AND s.name = 'MARUTHI DISTRIBUTOR';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'V018199', DATE '2026-11-30', 571.00, 58.24, 571.00, 25, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0032' AND s.name = 'SHRAVANI ENTERPRISES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'MIAT2401', DATE '2026-10-31', 15.70, 5.14, 15.70, 230, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0033' AND s.name = 'MIRAEE BIOTECH PRIVATE LIMITED';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'AIW01CYA', DATE '2027-08-31', 14.60, 9.73, 14.60, 540, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0034' AND s.name = 'VASU AGENCIES DRUGS PRIVATE LIMITED';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'AIW01CYB', DATE '2027-12-31', 16.06, 10.70, 16.06, 360, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0034' AND s.name = 'VASU AGENCIES DRUGS PRIVATE LIMITED';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'AIW01DBA', DATE '2027-09-30', 20.99, 13.99, 20.99, 297, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0035' AND s.name = 'VASU AGENCIES DRUGS PRIVATE LIMITED';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'AIW02DBB', DATE '2027-12-31', 20.99, 13.99, 20.99, 360, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0035' AND s.name = 'VASU AGENCIES DRUGS PRIVATE LIMITED';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '5S40417', DATE '2028-07-31', 17.58, 11.87, 17.58, 3, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0036' AND s.name = 'GANESH MEDICAL STORES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '25444385', DATE '2027-04-30', 19.55, 11.73, 19.55, 2, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0037' AND s.name = 'SERONIA LIFESCIENCES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '26440729', DATE '2028-01-31', 64.68, 41.40, 64.68, 1, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0038' AND s.name = 'GANESH MEDICAL STORES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'UPA16006', DATE '2027-08-31', 19.54, 11.16, 19.54, 2, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0039' AND s.name = 'VINAYAK ENTERPRISES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'BEL4006', DATE '2027-08-31', 26.30, 21.15, 26.30, 9, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0040' AND s.name = 'SHIV MEDICAL AGENCY';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'V4471514', DATE '2026-11-30', 46.90, 20.34, 46.90, 9, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0041' AND s.name = 'SRK SURGICALS';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'CDT25017', DATE '2027-04-30', 42.95, 27.49, 42.95, 20, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0042' AND s.name = 'KRISHNAM MEDICAL AGENCY';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'PCL0264', DATE '2027-03-31', 1.33, 0.85, 1.33, 33, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0043' AND s.name = 'SERONIA LIFESCIENCES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '24330041', DATE '2027-04-30', 71.80, 57.46, 71.80, 3, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0044' AND s.name = 'SHIV MEDICAL AGENCY';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '1244765', DATE '2026-09-30', 22.40, 18.76, 22.40, 20, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0045' AND s.name = 'SHRAVANI ENTERPRISES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'BO260156C', DATE '2028-04-30', 22.50, 15.00, 22.50, 230, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0046' AND s.name = 'VASU AGENCIES DRUGS PRIVATE LIMITED';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'I500014', DATE '2027-12-31', 1.35, 1.05, 1.35, 300, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0047' AND s.name = 'PARAS DISTRIBUTORS';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'GM0155319', DATE '2027-10-31', 192.70, 126.54, 192.70, 5, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0048' AND s.name = 'KRISHNAM MEDICAL AGENCY';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '25B01K25', DATE '2030-01-31', 27.50, 8.79, 27.50, 9, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0049' AND s.name = 'SHRAVANI ENTERPRISES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '26E07K69', DATE '2031-04-30', 28.13, 8.69, 28.13, 100, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0049' AND s.name = 'SHRAVANI ENTERPRISES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '428D27ND1', DATE '2029-08-31', 5.90, 2.41, 5.90, 98, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0050' AND s.name = 'SHRAVANI ENTERPRISES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '610027ANE1', DATE '2031-04-30', 6.04, 2.50, 6.04, 200, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0050' AND s.name = 'SHRAVANI ENTERPRISES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '622026NE2', DATE '2031-04-30', 6.04, 2.50, 6.04, 100, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0050' AND s.name = 'SHRAVANI ENTERPRISES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '4302421', DATE '2029-09-30', 10.00, 4.28, 10.00, 199, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0051' AND s.name = 'SHRAVANI ENTERPRISES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '623056EC1', DATE '2031-05-31', 10.23, 3.41, 10.23, 100, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0052' AND s.name = 'SHRAVANI ENTERPRISES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '416051NF2', DATE '2029-03-31', 7.50, 3.38, 7.50, 64, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0052' AND s.name = 'SRK SURGICALS';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '25F23K31', DATE '2030-05-31', 11.00, 4.32, 11.00, 73, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0053' AND s.name = 'SHRAVANI ENTERPRISES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'D0226033', DATE '2029-04-30', 41.07, 28.35, 41.07, 32, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0054' AND s.name = 'SHRAVANI ENTERPRISES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'DLSL344', DATE '2026-10-31', 40.99, 29.51, 40.99, 1, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0055' AND s.name = 'SERONIA LIFESCIENCES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'DLSL413', DATE '2028-04-30', 39.00, 28.08, 39.00, 5, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0055' AND s.name = 'SERONIA LIFESCIENCES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'DOBS4328', DATE '2029-12-31', 2.14, 1.29, 2.14, 28, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0056' AND s.name = 'JYOT PHARMA';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '25MN074K', DATE '2027-07-31', 34.99, 11.47, 34.99, 2, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0057' AND s.name = 'MIRAEE BIOTECH PRIVATE LIMITED';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'CCT251261B', DATE '2027-06-30', 8.90, 2.92, 8.90, 231, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0058' AND s.name = 'MIRAEE BIOTECH PRIVATE LIMITED';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'BD250164C', DATE '2026-12-31', 14.00, 4.89, 14.00, 5, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0059' AND s.name = 'MIRAEE BIOTECH PRIVATE LIMITED';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'BDZS0164C', DATE '2026-12-31', 14.00, 12.23, 14.00, 44, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0059' AND s.name = 'MIRAEE BIOTECH PRIVATE LIMITED';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'DXM2437', DATE '2027-09-30', 9.24, 7.39, 9.24, 45, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0060' AND s.name = 'GANESH MEDICAL STORES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '58CDS1587', DATE '2026-11-30', 13.30, 7.84, 13.30, 234, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0061' AND s.name = 'GANESH MEDICAL STORES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'TDMR-1851', DATE '2027-08-31', 24.14, 17.38, 24.14, 64, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0062' AND s.name = 'JYOT PHARMA';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'A06Z002', DATE '2028-12-31', 66.84, 26.74, 66.84, 60, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0063' AND s.name = 'GANESH MEDICAL STORES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'A06Y005', DATE '2028-01-31', 64.90, 33.58, 64.90, 5, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0063' AND s.name = 'SERONIA LIFESCIENCES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'PA26009', DATE '2028-01-31', 40.83, 24.50, 40.83, 50, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0064' AND s.name = 'KRISHNAM MEDICAL AGENCY';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '4SN1851', DATE '2027-08-31', 5.26, 4.21, 5.26, 18, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0065' AND s.name = 'GANESH MEDICAL STORES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '04011295', DATE '2027-03-31', 0.81, 0.67, 0.81, 350, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0066' AND s.name = 'SERONIA LIFESCIENCES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '04011202', DATE '2027-02-28', 0.39, 0.32, 0.39, 316, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0067' AND s.name = 'SERONIA LIFESCIENCES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '5I524', DATE '2027-11-30', 74.62, 53.73, 74.62, 10, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0068' AND s.name = 'GANESH MEDICAL STORES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '2GC24939A', DATE '2027-11-30', 15.70, 9.42, 15.70, 138, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0069' AND s.name = 'PAVAN AGENCIES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '18254640A', DATE '2028-10-31', 25.56, 19.42, 25.56, 29, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0070' AND s.name = 'SERONIA LIFESCIENCES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '18260719A', DATE '2028-01-31', 31.92, 23.47, 31.92, 41, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0071' AND s.name = 'GANESH MEDICAL STORES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'SIG2671C', DATE '2028-05-31', 18.50, 13.57, 18.50, 180, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0072' AND s.name = 'SERONIA LIFESCIENCES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'IOK0126001AI', DATE '2030-01-31', 19.85, 14.29, 19.85, 100, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0073' AND s.name = 'KRISHNAM MEDICAL AGENCY';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '5072C59911', DATE '2027-05-31', 4.34, 3.13, 4.34, 75, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0074' AND s.name = 'GANESH MEDICAL STORES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '18242750B', DATE '2028-09-30', 15.70, 11.30, 15.70, 75, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0075' AND s.name = 'SERONIA LIFESCIENCES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'TAB25131', DATE '2028-04-30', 19.55, 11.73, 19.55, 45, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0076' AND s.name = 'AADESH PHARMACEUTICALS';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'MX5282', DATE '2026-12-31', 1.72, 1.25, 1.72, 347, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0077' AND s.name = 'PAVAN AGENCIES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'BG19', DATE '2027-08-31', 560.00, 274.40, 560.00, 2, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0078' AND s.name = 'SHRAVANI ENTERPRISES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'BG63', DATE '2028-06-30', 560.00, 257.25, 560.00, 1, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0078' AND s.name = 'SHRAVANI ENTERPRISES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '12', DATE '2027-01-31', 540.00, 280.00, 540.00, 1, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0078' AND s.name = 'SRK SURGICALS';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'M2342015', DATE '2027-01-31', 275.00, 65.10, 275.00, 10, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0079' AND s.name = 'SHRAVANI ENTERPRISES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'LTA-54050', DATE '2029-04-30', 9.93, 3.25, 9.93, 420, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0080' AND s.name = 'MIRAEE BIOTECH PRIVATE LIMITED';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '1', DATE '2030-04-30', 15.00, 2.99, 15.00, 100, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0081' AND s.name = 'SHRAVANI ENTERPRISES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '7261-MB052', DATE '2030-12-31', 14.00, 2.99, 14.00, 100, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0081' AND s.name = 'SHRAVANI ENTERPRISES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '0720231750', DATE '2028-07-31', 12.00, 2.80, 12.00, 100, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0081' AND s.name = 'SRK SURGICALS';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'AJM0012', DATE '2028-12-31', 2.22, 1.42, 2.22, 70, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0082' AND s.name = 'JYOT PHARMA';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'B5AKZ004', DATE '2027-12-31', 20.44, 12.58, 20.44, 65, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0083' AND s.name = 'KRISHNAM MEDICAL AGENCY';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'A3AEZ029', DATE '2027-08-31', 34.50, 21.16, 34.50, 91, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0084' AND s.name = 'KRISHNAM MEDICAL AGENCY';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'AKM0020A', DATE '2028-03-31', 145.75, 99.12, 145.75, 10, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0085' AND s.name = 'KRISHNAM MEDICAL AGENCY';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'J05Y013', DATE '2027-11-30', 12.35, 8.65, 12.35, 69, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0086' AND s.name = 'JYOT PHARMA';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'J05Z001', DATE '2027-12-31', 12.35, 8.89, 12.35, 30, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0086' AND s.name = 'SERONIA LIFESCIENCES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'ZT2419K', DATE '2026-10-31', 13.50, 5.40, 13.50, 140, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0087' AND s.name = 'HYGEN HEALTH PRIVATE LIMITED';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '1R2X004', DATE '2027-04-30', 273.90, 182.61, 273.90, 5, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0088' AND s.name = 'HARMA PLUS DISTRIBUTOR PRIVATE LIMITED';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'SH25090886', DATE '2027-08-31', 435.94, 313.87, 435.94, 1, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0089' AND s.name = 'GANESH MEDICAL STORES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'AFB24554', DATE '2027-03-31', 1.83, 1.21, 1.83, 38, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0090' AND s.name = 'PARAS DISTRIBUTORS';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'CPL50475', DATE '2027-08-31', 60.39, 38.82, 60.39, 5, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0091' AND s.name = 'SERONIA LIFESCIENCES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'INA24007', DATE '2027-08-31', 1.47, 1.17, 1.47, 48, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0092' AND s.name = 'SERONIA LIFESCIENCES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '2GC24405A', DATE '2027-05-31', 9.18, 5.65, 9.18, 55, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0093' AND s.name = 'GANESH MEDICAL STORES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '2GC25666A', DATE '2028-07-31', 18.23, 9.76, 18.23, 33, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0094' AND s.name = 'GANESH MEDICAL STORES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '43554N', DATE '2029-07-31', 198.00, 16.80, 198.00, 56, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0095' AND s.name = 'SRK SURGICALS';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '43832N', DATE '2029-08-31', 198.00, 16.80, 198.00, 90, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0096' AND s.name = 'SRK SURGICALS';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'EFME03-0925', DATE '2028-08-31', 47.00, 9.85, 47.00, 74, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0097' AND s.name = 'SHRAVANI ENTERPRISES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'G26C020663', DATE '2031-02-28', 150.00, 16.28, 150.00, 24, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0098' AND s.name = 'SHRAVANI ENTERPRISES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '1173', DATE '2027-09-30', 370.00, 239.40, 370.00, 1, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0099' AND s.name = 'SHRAVANI ENTERPRISES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '1318', DATE '2028-02-29', 370.00, 239.40, 370.00, 1, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0099' AND s.name = 'SHRAVANI ENTERPRISES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '601', DATE '2026-11-30', 400.00, 215.04, 400.00, 1, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0099' AND s.name = 'SHRAVANI ENTERPRISES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '25420095', DATE '2028-11-30', 10.41, 6.66, 10.41, 245, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0100' AND s.name = 'GANESH MEDICAL STORES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'V240767', DATE '2027-08-31', 36.59, 23.42, 36.59, 47, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0101' AND s.name = 'PARAS DISTRIBUTORS';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '240624', DATE '2029-06-30', 3.50, 1.28, 3.50, 100, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0102' AND s.name = 'SERONIA LIFESCIENCES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'GT25724A', DATE '2028-05-31', 6.10, 3.81, 6.10, 91, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0103' AND s.name = 'GANESH MEDICAL STORES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'PAJA5938', DATE '2027-11-30', 1.65, 1.24, 1.65, 140, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0104' AND s.name = 'KRISHNAM MEDICAL AGENCY';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '6110C84206', DATE '2028-03-31', 6.96, 5.01, 6.96, 30, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0105' AND s.name = 'GANESH MEDICAL STORES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'LOAS0070', DATE '2026-11-30', 10.93, 6.86, 10.93, 23, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0106' AND s.name = 'PARAS DISTRIBUTORS';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'SM144578', DATE '2026-09-30', 34.93, 30.58, 34.93, 10, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0107' AND s.name = 'SRK SURGICALS';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'JL-3720', DATE '2028-01-31', 350.42, 195.53, 350.42, 3, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0108' AND s.name = 'PARAS DISTRIBUTORS';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '4027171H', DATE '2027-01-31', 91.67, 18.67, 91.67, 9, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0109' AND s.name = 'SRK SURGICALS';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '4057232P', DATE '2027-04-30', 183.33, 37.33, 183.33, 6, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0110' AND s.name = 'SRK SURGICALS';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'ZQL25137', DATE '2027-11-30', 61.87, 41.25, 61.87, 5, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0111' AND s.name = 'JYOT PHARMA';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'YMS2608', DATE '2028-12-31', 5.16, 3.09, 5.16, 71, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0112' AND s.name = 'JYOT PHARMA';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'PZ24155', DATE '2026-09-30', 37.00, 25.84, 37.00, 1, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0113' AND s.name = 'PARAS DISTRIBUTORS';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'SPF260771', DATE '2028-05-31', 17.90, 12.89, 17.90, 100, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0114' AND s.name = 'KRISHNAM MEDICAL AGENCY';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '2250475', DATE '2027-12-31', 23.52, 18.20, 23.52, 87, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0115' AND s.name = 'SHRAVANI ENTERPRISES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'TM824208', DATE '2028-07-31', 1.70, 1.32, 1.70, 127, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0116' AND s.name = 'GANESH MEDICAL STORES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'E9AIYO47', DATE '2027-09-30', 93.64, 68.16, 93.64, 1, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0117' AND s.name = 'SERONIA LIFESCIENCES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'ATS42AJA', DATE '2027-11-30', 17.44, 12.56, 17.44, 85, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0118' AND s.name = 'KRISHNAM MEDICAL AGENCY';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'TB0498A', DATE '2029-03-31', 72.98, 48.65, 72.98, 6, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0119' AND s.name = 'KRISHNAM MEDICAL AGENCY';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'TA0195A', DATE '2028-04-30', 70.78, 46.00, 70.78, 1, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0119' AND s.name = 'SHIV MEDICAL AGENCY';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'FHD0132', DATE '2029-01-31', 11.75, 8.62, 11.75, 129, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0120' AND s.name = 'SERONIA LIFESCIENCES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'R615', DATE '2026-10-31', 103.95, 83.16, 103.95, 2, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0121' AND s.name = 'SERONIA LIFESCIENCES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'K2502648', DATE '2028-10-31', 8.66, 6.24, 8.66, 90, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0122' AND s.name = 'JYOT PHARMA';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '2250788', DATE '2027-07-31', 22.04, 15.18, 22.04, 49, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0123' AND s.name = 'SHRAVANI ENTERPRISES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '2261288', DATE '2029-03-31', 21.16, 15.54, 21.16, 100, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0123' AND s.name = 'SHRAVANI ENTERPRISES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'IA250164', DATE '2027-12-31', 22.03, 16.45, 22.03, 1, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0123' AND s.name = 'SRK SURGICALS';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'D0126054', DATE '2029-03-31', 37.25, 27.30, 37.25, 24, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0124' AND s.name = 'SHRAVANI ENTERPRISES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'F25Z003', DATE '2028-02-29', 10.61, 8.06, 10.61, 70, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0125' AND s.name = 'JYOT PHARMA';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'F25Z005', DATE '2028-02-29', 10.61, 8.06, 10.61, 5, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0125' AND s.name = 'SERONIA LIFESCIENCES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'E260648', DATE '2030-04-30', 21.40, 13.69, 21.40, 100, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0126' AND s.name = 'KRISHNAM MEDICAL AGENCY';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'E50281', DATE '2029-02-28', 20.77, 13.29, 20.77, 2, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0126' AND s.name = 'KRISHNAM MEDICAL AGENCY';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'IA01669A', DATE '2027-06-30', 3.07, 2.10, 3.07, 46, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0127' AND s.name = 'GANESH MEDICAL STORES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '18242120B', DATE '2027-08-31', 79.80, 60.01, 79.80, 4, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0128' AND s.name = 'SERONIA LIFESCIENCES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '18261188A', DATE '2030-02-28', 10.04, 6.42, 10.04, 59, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0129' AND s.name = 'SERONIA LIFESCIENCES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '18254295A', DATE '2028-09-30', 64.63, 48.60, 64.63, 3, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0130' AND s.name = 'SERONIA LIFESCIENCES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '0182501F', DATE '2027-05-31', 23.76, 14.85, 23.76, 360, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0131' AND s.name = 'VASU AGENCIES DRUGS PRIVATE LIMITED';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '0342501F', DATE '2027-05-31', 22.50, 18.00, 22.50, 3, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0132' AND s.name = 'OPENING STOCK';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '0122402L', DATE '2026-11-30', 16.83, 10.52, 16.83, 554, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0133' AND s.name = 'VASU AGENCIES DRUGS PRIVATE LIMITED';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '13251830A', DATE '2029-11-30', 1.28, 1.01, 1.28, 3, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0134' AND s.name = 'GANESH MEDICAL STORES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'OND25280SR', DATE '2027-09-30', 12.73, 5.09, 12.73, 27, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0135' AND s.name = 'SERONIA LIFESCIENCES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '25444311', DATE '2027-10-31', 5.49, 3.95, 5.49, 27, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0136' AND s.name = 'SERONIA LIFESCIENCES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'HH2602066', DATE '2028-01-31', 15.85, 9.14, 15.85, 6, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0137' AND s.name = 'KRISHNAM MEDICAL AGENCY';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'PX26074', DATE '2028-03-31', 132.86, 104.08, 132.86, 3, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0138' AND s.name = 'KRISHNAM MEDICAL AGENCY';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'PP24028', DATE '2027-01-31', 46.92, 35.85, 46.92, 5, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0139' AND s.name = 'GANESH MEDICAL STORES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'LY0853', DATE '2027-04-30', 3.17, 3.06, 3.17, 21, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0140' AND s.name = 'GANESH MEDICAL STORES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'PFT25088', DATE '2028-10-31', 0.97, 0.69, 0.97, 272, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0141' AND s.name = 'SERONIA LIFESCIENCES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'GR284066AZ', DATE '2027-04-30', 1.01, 0.74, 1.01, 4, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0142' AND s.name = 'PARAS DISTRIBUTORS';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '26440423', DATE '2027-07-31', 29.63, 21.92, 29.63, 5, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0143' AND s.name = 'SERONIA LIFESCIENCES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '376025001', DATE '2027-10-31', 15.90, 5.21, 15.90, 107, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0144' AND s.name = 'MIRAEE BIOTECH PRIVATE LIMITED';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'T26E611D', DATE '2028-04-30', 11.70, 4.65, 11.70, 69, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0145' AND s.name = 'RAINBOW GLOBAL HEALTHCARE PVT LTD';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'Z25-910', DATE '2028-07-31', 544.00, 31.36, 544.00, 5, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0146' AND s.name = 'SHRAVANI ENTERPRISES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'JKFD25017', DATE '2028-08-31', 9.58, 1.92, 9.58, 2, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0147' AND s.name = 'SERONIA LIFESCIENCES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'KHCHP25', DATE '2026-10-31', 20.00, 7.84, 20.00, 5, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0148' AND s.name = 'SRK SURGICALS';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'CPCA4060A', DATE '2026-11-30', 39.80, 19.23, 39.80, 2, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0149' AND s.name = 'SREEZEN PHARMA';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '251149', DATE '2027-10-31', 513.00, 260.40, 513.00, 1, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0150' AND s.name = 'SHRAVANI ENTERPRISES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'PKL24022N', DATE '2026-10-31', 215.30, 156.80, 215.30, 1, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0150' AND s.name = 'SRK SURGICALS';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'B7HTX039', DATE '2027-07-31', 60.00, 24.00, 60.00, 52, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0151' AND s.name = 'SERONIA LIFESCIENCES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'GBUF0008', DATE '2027-07-31', 27.42, 18.92, 27.42, 16, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0152' AND s.name = 'GANESH MEDICAL STORES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'SL26371', DATE '2028-06-30', 116.00, 46.54, 116.00, 11, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0153' AND s.name = 'RAINBOW GLOBAL HEALTHCARE PVT LTD';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'B26F015C', DATE '2027-11-30', 28.80, 14.42, 28.80, 67, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0154' AND s.name = 'RAINBOW GLOBAL HEALTHCARE PVT LTD';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'T26E665F', DATE '2028-04-30', 14.80, 5.89, 14.80, 4, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0155' AND s.name = 'RAINBOW GLOBAL HEALTHCARE PVT LTD';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'L26F089A', DATE '2027-05-31', 158.00, 60.56, 158.00, 5, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0156' AND s.name = 'RAINBOW GLOBAL HEALTHCARE PVT LTD';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'GEM0015', DATE '2028-01-31', 45.67, 33.98, 45.67, 80, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0157' AND s.name = 'KRISHNAM MEDICAL AGENCY';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'AIW01AYB', DATE '2028-01-31', 19.32, 12.88, 19.32, 240, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0158' AND s.name = 'VASU AGENCIES DRUGS PRIVATE LIMITED';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'AIW06AYA', DATE '2027-04-30', 19.32, 12.88, 19.32, 310, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0158' AND s.name = 'VASU AGENCIES DRUGS PRIVATE LIMITED';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'AIW01ALB', DATE '2028-03-31', 24.71, 16.47, 24.71, 300, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0159' AND s.name = 'VASU AGENCIES DRUGS PRIVATE LIMITED';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'D0426044', DATE '2029-04-30', 60.74, 33.60, 60.74, 16, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0160' AND s.name = 'SHRAVANI ENTERPRISES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '120', DATE '2028-01-31', 12.00, 6.09, 12.00, 51, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0161' AND s.name = 'SHRAVANI ENTERPRISES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '95', DATE '2028-10-31', 12.00, 6.16, 12.00, 10, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0161' AND s.name = 'SHRAVANI ENTERPRISES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '113', DATE '2028-10-31', 18.00, 9.07, 18.00, 1, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0162' AND s.name = 'SHRAVANI ENTERPRISES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'MANT2401', DATE '2026-10-31', 30.00, 26.20, 30.00, 100, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0163' AND s.name = 'MIRAEE BIOTECH PRIVATE LIMITED';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '220510711', DATE '2027-04-30', 25.00, 7.28, 25.00, 47, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0164' AND s.name = 'SHRAVANI ENTERPRISES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'SYL2607', DATE '2028-02-29', 161.38, 107.59, 161.38, 4, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0165' AND s.name = 'GANESH MEDICAL STORES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'MPM244836', DATE '2026-11-30', 22.77, 16.05, 22.77, 10, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0166' AND s.name = 'GANESH MEDICAL STORES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'S4894', DATE '2027-11-30', 156.00, 114.82, 156.00, 1, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0167' AND s.name = 'GANESH MEDICAL STORES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'A26110MU', DATE '2028-01-31', 256.50, 182.40, 256.50, 4, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0168' AND s.name = 'MARUTHI DISTRIBUTOR';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'N2601043', DATE '2029-03-31', 14.44, 10.51, 14.44, 3, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0169' AND s.name = 'GANESH MEDICAL STORES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'BDS26010', DATE '2028-02-29', 8.97, 6.24, 8.97, 50, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0170' AND s.name = 'JYOT PHARMA';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'BDS25008', DATE '2027-02-28', 10.87, 7.46, 10.87, 28, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0171' AND s.name = 'GANESH MEDICAL STORES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'E2402350', DATE '2027-08-31', 2.80, 2.01, 2.80, 60, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0172' AND s.name = 'SERONIA LIFESCIENCES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'SG-D2424', DATE '2027-03-31', 75.00, 28.00, 75.00, 7, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0173' AND s.name = 'SRK SURGICALS';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'SAC1001225', DATE '2028-05-31', 145.31, 106.95, 145.31, 1, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0174' AND s.name = 'GANESH MEDICAL STORES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'KSS1014', DATE '2026-10-31', 750.00, 21.84, 750.00, 10, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0175' AND s.name = 'SRK SURGICALS';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '24B3006M', DATE '2029-03-31', 80.00, 25.76, 80.00, 24, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0176' AND s.name = 'SRK SURGICALS';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '24C0644KK', DATE '2029-02-28', 88.00, 25.76, 88.00, 25, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0177' AND s.name = 'SRK SURGICALS';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'GKF1577A', DATE '2026-09-30', 69.50, 52.27, 69.50, 4, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0178' AND s.name = 'GANESH MEDICAL STORES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'GKG0727A', DATE '2027-03-31', 69.50, 52.27, 69.50, 15, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0178' AND s.name = 'GANESH MEDICAL STORES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '25461972', DATE '2027-09-30', 39.94, 24.58, 39.94, 8, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0179' AND s.name = 'SERONIA LIFESCIENCES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '25180259', DATE '2027-09-30', 45.52, 29.13, 45.52, 8, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0180' AND s.name = 'GANESH MEDICAL STORES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '25460734', DATE '2027-03-31', 12.50, 9.00, 12.50, 94, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0181' AND s.name = 'SERONIA LIFESCIENCES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '26460180', DATE '2028-01-31', 10.44, 7.51, 10.44, 50, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0182' AND s.name = 'GANESH MEDICAL STORES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '24462099', DATE '2026-11-30', 9.90, 6.70, 9.90, 2, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0183' AND s.name = 'PARAS DISTRIBUTORS';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '48019916', DATE '2026-12-31', 3.88, 2.84, 3.88, 5, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0184' AND s.name = 'SERONIA LIFESCIENCES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '18250387', DATE '2027-06-30', 20.33, 14.05, 20.33, 45, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0185' AND s.name = 'GANESH MEDICAL STORES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '5KSZ013', DATE '2027-12-31', 4.12, 3.13, 4.12, 245, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0186' AND s.name = 'SERONIA LIFESCIENCES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'B55Y020', DATE '2027-03-31', 4.40, 3.34, 4.40, 7, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0187' AND s.name = 'SERONIA LIFESCIENCES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'MKQ25001', DATE '2027-12-31', 7.50, 2.46, 7.50, 480, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0188' AND s.name = 'MIRAEE BIOTECH PRIVATE LIMITED';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'CCT25816', DATE '2027-04-30', 12.00, 3.93, 12.00, 20, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0189' AND s.name = 'MIRAEE BIOTECH PRIVATE LIMITED';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'CCT260994B', DATE '2028-03-31', 12.19, 4.26, 12.19, 380, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0190' AND s.name = 'MIRAEE BIOTECH PRIVATE LIMITED';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'KN25138', DATE '2028-11-30', 13.32, 10.48, 13.32, 100, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0191' AND s.name = 'SHRAVANI ENTERPRISES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '2332C', DATE '2029-09-30', 13.05, 9.29, 13.05, 5, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0191' AND s.name = 'SRK SURGICALS';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'TMH26033', DATE '2028-01-31', 168.82, 126.95, 168.82, 2, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0192' AND s.name = 'SERONIA LIFESCIENCES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'AEM0130', DATE '2028-02-29', 189.72, 144.19, 189.72, 1, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0193' AND s.name = 'SERONIA LIFESCIENCES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'TMB26080', DATE '2028-01-31', 185.40, 137.94, 185.40, 2, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0194' AND s.name = 'SERONIA LIFESCIENCES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'TMD25458', DATE '2027-11-30', 132.02, 98.22, 132.02, 2, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0195' AND s.name = 'SERONIA LIFESCIENCES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '16251371A', DATE '2027-09-30', 168.83, 126.96, 168.83, 1, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0196' AND s.name = 'SERONIA LIFESCIENCES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '16251332A', DATE '2027-09-30', 189.72, 141.20, 189.72, 1, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0197' AND s.name = 'GANESH MEDICAL STORES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '16250316A', DATE '2027-02-28', 227.13, 168.99, 227.13, 2, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0198' AND s.name = 'GANESH MEDICAL STORES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '16260495A', DATE '2028-02-29', 187.63, 142.60, 187.63, 1, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0199' AND s.name = 'SERONIA LIFESCIENCES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '13241356A', DATE '2026-11-30', 180.32, 134.16, 180.32, 3, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0200' AND s.name = 'SERONIA LIFESCIENCES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '16251397A', DATE '2027-09-30', 183.97, 138.35, 183.97, 2, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0201' AND s.name = 'SERONIA LIFESCIENCES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '16250019A', DATE '2026-12-31', 221.50, 163.92, 221.50, 2, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0202' AND s.name = 'SERONIA LIFESCIENCES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'YPIBG09', DATE '2026-11-30', 16.78, 13.42, 16.78, 10, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0203' AND s.name = 'GANESH MEDICAL STORES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'ANO10AFA', DATE '2027-10-31', 27.14, 20.19, 27.14, 30, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0204' AND s.name = 'GANESH MEDICAL STORES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'B26F002C', DATE '2027-11-30', 19.60, 9.81, 19.60, 168, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0205' AND s.name = 'RAINBOW GLOBAL HEALTHCARE PVT LTD';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '18242203A', DATE '2027-08-31', 19.47, 14.02, 19.47, 26, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0206' AND s.name = 'GANESH MEDICAL STORES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'WTYZ325004', DATE '2027-11-30', 20.41, 13.61, 20.41, 410, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0207' AND s.name = 'VASU AGENCIES DRUGS PRIVATE LIMITED';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'HSP26006', DATE '2027-12-31', 12.99, 8.66, 12.99, 395, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0208' AND s.name = 'VASU AGENCIES DRUGS PRIVATE LIMITED';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '753022D7', DATE '2028-02-29', 35.14, 26.14, 35.14, 65, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0209' AND s.name = 'GANESH MEDICAL STORES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '802922D7', DATE '2028-07-31', 61.60, 44.35, 61.60, 90, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0210' AND s.name = 'GANESH MEDICAL STORES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'TUR-2269', DATE '2026-09-30', 36.81, 27.24, 36.81, 29, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0211' AND s.name = 'SERONIA LIFESCIENCES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'VEB26010', DATE '2028-12-31', 27.06, 20.57, 27.06, 138, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0212' AND s.name = 'SHREEJI PHARMA';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'BA26102', DATE '2028-04-30', 22.83, 16.44, 22.83, 100, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0213' AND s.name = 'KRISHNAM MEDICAL AGENCY';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'BA26T01', DATE '2028-01-31', 21.95, 15.81, 21.95, 18, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0213' AND s.name = 'KRISHNAM MEDICAL AGENCY';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'D45Z007', DATE '2028-02-29', 4.81, 2.96, 4.81, 130, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0214' AND s.name = 'JYOT PHARMA';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'NM1735', DATE '2027-10-31', 1.27, 1.01, 1.27, 25, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0215' AND s.name = 'GANESH MEDICAL STORES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, '0062', DATE '2028-03-31', 108.75, 72.50, 108.75, 1, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0216' AND s.name = 'GANESH MEDICAL STORES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'GIF036001', DATE '2028-01-31', 13.57, 9.04, 13.57, 6, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0217' AND s.name = 'VASU AGENCIES DRUGS PRIVATE LIMITED';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'SXF2222A', DATE '2027-09-30', 10.90, 7.61, 10.90, 9, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0218' AND s.name = 'PARAS DISTRIBUTORS';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'CSW1025005E', DATE '2027-07-31', 6.72, 4.95, 6.72, 35, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0219' AND s.name = 'SERONIA LIFESCIENCES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'DSB1026003A', DATE '2029-02-28', 17.45, 12.98, 17.45, 50, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0220' AND s.name = 'GANESH MEDICAL STORES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'FXT0126003AS', DATE '2029-02-28', 27.50, 20.46, 27.50, 41, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0221' AND s.name = 'GANESH MEDICAL STORES';
INSERT INTO medicine_batches (medicine_id, batch_no, expiry_date, mrp, purchase_rate, sale_rate, quantity_on_hand, supplier_id, source_purchase_item_id, created_date)
SELECT m.id, 'ZVT26121', DATE '2027-10-31', 7.18, 4.79, 7.18, 47, s.id, NULL, CURRENT_TIMESTAMP
FROM medicines m, suppliers s
WHERE m.code = 'MED-0222' AND s.name = 'GANESH MEDICAL STORES';

-- ---------------------------------------------------------------
-- STOCK_LEDGER_ENTRIES (249 rows - one opening-balance entry per
-- medicine_batches row created above, matched back via the
-- unique (medicine_id, batch_no) pair)
-- ---------------------------------------------------------------
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0001' AND mb.batch_no = '0726A021';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0002' AND mb.batch_no = '072C024';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0003' AND mb.batch_no = '1245428';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0004' AND mb.batch_no = 'E51437';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0005' AND mb.batch_no = 'K2AIY069';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0006' AND mb.batch_no = 'G55Y024';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0007' AND mb.batch_no = 'A1AJX175';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0008' AND mb.batch_no = '11241612';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0009' AND mb.batch_no = '05251631A';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0010' AND mb.batch_no = '11251682';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0011' AND mb.batch_no = 'U2AIY025';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0012' AND mb.batch_no = 'E2402669';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0013' AND mb.batch_no = 'E2600053';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0014' AND mb.batch_no = 'AATI25004';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0015' AND mb.batch_no = 'IA00084A';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0016' AND mb.batch_no = '826D182';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0017' AND mb.batch_no = '6SN0153';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0018' AND mb.batch_no = '2408001433';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0019' AND mb.batch_no = '2508001017';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0020' AND mb.batch_no = 'XXXX';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0021' AND mb.batch_no = 'XXXXX';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0022' AND mb.batch_no = 'A6AGX036';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0023' AND mb.batch_no = 'GF00126';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0024' AND mb.batch_no = 'EY26869004';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0025' AND mb.batch_no = 'AXC0179';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0026' AND mb.batch_no = 'RHL41';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0027' AND mb.batch_no = 'TT25031';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0028' AND mb.batch_no = '50230066';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0029' AND mb.batch_no = 'AIW02CHA';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0030' AND mb.batch_no = 'CTH26002';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0031' AND mb.batch_no = 'A25045ZP';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0032' AND mb.batch_no = 'V018199';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0033' AND mb.batch_no = 'MIAT2401';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0034' AND mb.batch_no = 'AIW01CYA';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0034' AND mb.batch_no = 'AIW01CYB';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0035' AND mb.batch_no = 'AIW01DBA';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0035' AND mb.batch_no = 'AIW02DBB';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0036' AND mb.batch_no = '5S40417';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0037' AND mb.batch_no = '25444385';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0038' AND mb.batch_no = '26440729';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0039' AND mb.batch_no = 'UPA16006';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0040' AND mb.batch_no = 'BEL4006';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0041' AND mb.batch_no = 'V4471514';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0042' AND mb.batch_no = 'CDT25017';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0043' AND mb.batch_no = 'PCL0264';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0044' AND mb.batch_no = '24330041';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0045' AND mb.batch_no = '1244765';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0046' AND mb.batch_no = 'BO260156C';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0047' AND mb.batch_no = 'I500014';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0048' AND mb.batch_no = 'GM0155319';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0049' AND mb.batch_no = '25B01K25';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0049' AND mb.batch_no = '26E07K69';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0050' AND mb.batch_no = '428D27ND1';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0050' AND mb.batch_no = '610027ANE1';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0050' AND mb.batch_no = '622026NE2';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0051' AND mb.batch_no = '4302421';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0052' AND mb.batch_no = '623056EC1';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0052' AND mb.batch_no = '416051NF2';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0053' AND mb.batch_no = '25F23K31';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0054' AND mb.batch_no = 'D0226033';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0055' AND mb.batch_no = 'DLSL344';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0055' AND mb.batch_no = 'DLSL413';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0056' AND mb.batch_no = 'DOBS4328';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0057' AND mb.batch_no = '25MN074K';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0058' AND mb.batch_no = 'CCT251261B';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0059' AND mb.batch_no = 'BD250164C';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0059' AND mb.batch_no = 'BDZS0164C';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0060' AND mb.batch_no = 'DXM2437';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0061' AND mb.batch_no = '58CDS1587';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0062' AND mb.batch_no = 'TDMR-1851';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0063' AND mb.batch_no = 'A06Z002';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0063' AND mb.batch_no = 'A06Y005';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0064' AND mb.batch_no = 'PA26009';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0065' AND mb.batch_no = '4SN1851';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0066' AND mb.batch_no = '04011295';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0067' AND mb.batch_no = '04011202';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0068' AND mb.batch_no = '5I524';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0069' AND mb.batch_no = '2GC24939A';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0070' AND mb.batch_no = '18254640A';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0071' AND mb.batch_no = '18260719A';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0072' AND mb.batch_no = 'SIG2671C';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0073' AND mb.batch_no = 'IOK0126001AI';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0074' AND mb.batch_no = '5072C59911';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0075' AND mb.batch_no = '18242750B';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0076' AND mb.batch_no = 'TAB25131';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0077' AND mb.batch_no = 'MX5282';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0078' AND mb.batch_no = 'BG19';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0078' AND mb.batch_no = 'BG63';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0078' AND mb.batch_no = '12';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0079' AND mb.batch_no = 'M2342015';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0080' AND mb.batch_no = 'LTA-54050';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0081' AND mb.batch_no = '1';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0081' AND mb.batch_no = '7261-MB052';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0081' AND mb.batch_no = '0720231750';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0082' AND mb.batch_no = 'AJM0012';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0083' AND mb.batch_no = 'B5AKZ004';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0084' AND mb.batch_no = 'A3AEZ029';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0085' AND mb.batch_no = 'AKM0020A';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0086' AND mb.batch_no = 'J05Y013';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0086' AND mb.batch_no = 'J05Z001';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0087' AND mb.batch_no = 'ZT2419K';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0088' AND mb.batch_no = '1R2X004';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0089' AND mb.batch_no = 'SH25090886';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0090' AND mb.batch_no = 'AFB24554';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0091' AND mb.batch_no = 'CPL50475';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0092' AND mb.batch_no = 'INA24007';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0093' AND mb.batch_no = '2GC24405A';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0094' AND mb.batch_no = '2GC25666A';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0095' AND mb.batch_no = '43554N';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0096' AND mb.batch_no = '43832N';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0097' AND mb.batch_no = 'EFME03-0925';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0098' AND mb.batch_no = 'G26C020663';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0099' AND mb.batch_no = '1173';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0099' AND mb.batch_no = '1318';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0099' AND mb.batch_no = '601';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0100' AND mb.batch_no = '25420095';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0101' AND mb.batch_no = 'V240767';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0102' AND mb.batch_no = '240624';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0103' AND mb.batch_no = 'GT25724A';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0104' AND mb.batch_no = 'PAJA5938';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0105' AND mb.batch_no = '6110C84206';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0106' AND mb.batch_no = 'LOAS0070';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0107' AND mb.batch_no = 'SM144578';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0108' AND mb.batch_no = 'JL-3720';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0109' AND mb.batch_no = '4027171H';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0110' AND mb.batch_no = '4057232P';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0111' AND mb.batch_no = 'ZQL25137';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0112' AND mb.batch_no = 'YMS2608';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0113' AND mb.batch_no = 'PZ24155';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0114' AND mb.batch_no = 'SPF260771';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0115' AND mb.batch_no = '2250475';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0116' AND mb.batch_no = 'TM824208';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0117' AND mb.batch_no = 'E9AIYO47';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0118' AND mb.batch_no = 'ATS42AJA';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0119' AND mb.batch_no = 'TB0498A';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0119' AND mb.batch_no = 'TA0195A';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0120' AND mb.batch_no = 'FHD0132';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0121' AND mb.batch_no = 'R615';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0122' AND mb.batch_no = 'K2502648';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0123' AND mb.batch_no = '2250788';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0123' AND mb.batch_no = '2261288';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0123' AND mb.batch_no = 'IA250164';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0124' AND mb.batch_no = 'D0126054';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0125' AND mb.batch_no = 'F25Z003';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0125' AND mb.batch_no = 'F25Z005';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0126' AND mb.batch_no = 'E260648';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0126' AND mb.batch_no = 'E50281';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0127' AND mb.batch_no = 'IA01669A';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0128' AND mb.batch_no = '18242120B';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0129' AND mb.batch_no = '18261188A';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0130' AND mb.batch_no = '18254295A';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0131' AND mb.batch_no = '0182501F';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0132' AND mb.batch_no = '0342501F';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0133' AND mb.batch_no = '0122402L';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0134' AND mb.batch_no = '13251830A';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0135' AND mb.batch_no = 'OND25280SR';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0136' AND mb.batch_no = '25444311';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0137' AND mb.batch_no = 'HH2602066';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0138' AND mb.batch_no = 'PX26074';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0139' AND mb.batch_no = 'PP24028';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0140' AND mb.batch_no = 'LY0853';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0141' AND mb.batch_no = 'PFT25088';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0142' AND mb.batch_no = 'GR284066AZ';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0143' AND mb.batch_no = '26440423';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0144' AND mb.batch_no = '376025001';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0145' AND mb.batch_no = 'T26E611D';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0146' AND mb.batch_no = 'Z25-910';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0147' AND mb.batch_no = 'JKFD25017';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0148' AND mb.batch_no = 'KHCHP25';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0149' AND mb.batch_no = 'CPCA4060A';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0150' AND mb.batch_no = '251149';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0150' AND mb.batch_no = 'PKL24022N';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0151' AND mb.batch_no = 'B7HTX039';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0152' AND mb.batch_no = 'GBUF0008';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0153' AND mb.batch_no = 'SL26371';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0154' AND mb.batch_no = 'B26F015C';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0155' AND mb.batch_no = 'T26E665F';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0156' AND mb.batch_no = 'L26F089A';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0157' AND mb.batch_no = 'GEM0015';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0158' AND mb.batch_no = 'AIW01AYB';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0158' AND mb.batch_no = 'AIW06AYA';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0159' AND mb.batch_no = 'AIW01ALB';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0160' AND mb.batch_no = 'D0426044';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0161' AND mb.batch_no = '120';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0161' AND mb.batch_no = '95';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0162' AND mb.batch_no = '113';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0163' AND mb.batch_no = 'MANT2401';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0164' AND mb.batch_no = '220510711';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0165' AND mb.batch_no = 'SYL2607';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0166' AND mb.batch_no = 'MPM244836';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0167' AND mb.batch_no = 'S4894';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0168' AND mb.batch_no = 'A26110MU';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0169' AND mb.batch_no = 'N2601043';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0170' AND mb.batch_no = 'BDS26010';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0171' AND mb.batch_no = 'BDS25008';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0172' AND mb.batch_no = 'E2402350';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0173' AND mb.batch_no = 'SG-D2424';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0174' AND mb.batch_no = 'SAC1001225';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0175' AND mb.batch_no = 'KSS1014';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0176' AND mb.batch_no = '24B3006M';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0177' AND mb.batch_no = '24C0644KK';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0178' AND mb.batch_no = 'GKF1577A';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0178' AND mb.batch_no = 'GKG0727A';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0179' AND mb.batch_no = '25461972';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0180' AND mb.batch_no = '25180259';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0181' AND mb.batch_no = '25460734';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0182' AND mb.batch_no = '26460180';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0183' AND mb.batch_no = '24462099';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0184' AND mb.batch_no = '48019916';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0185' AND mb.batch_no = '18250387';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0186' AND mb.batch_no = '5KSZ013';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0187' AND mb.batch_no = 'B55Y020';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0188' AND mb.batch_no = 'MKQ25001';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0189' AND mb.batch_no = 'CCT25816';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0190' AND mb.batch_no = 'CCT260994B';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0191' AND mb.batch_no = 'KN25138';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0191' AND mb.batch_no = '2332C';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0192' AND mb.batch_no = 'TMH26033';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0193' AND mb.batch_no = 'AEM0130';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0194' AND mb.batch_no = 'TMB26080';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0195' AND mb.batch_no = 'TMD25458';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0196' AND mb.batch_no = '16251371A';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0197' AND mb.batch_no = '16251332A';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0198' AND mb.batch_no = '16250316A';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0199' AND mb.batch_no = '16260495A';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0200' AND mb.batch_no = '13241356A';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0201' AND mb.batch_no = '16251397A';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0202' AND mb.batch_no = '16250019A';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0203' AND mb.batch_no = 'YPIBG09';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0204' AND mb.batch_no = 'ANO10AFA';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0205' AND mb.batch_no = 'B26F002C';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0206' AND mb.batch_no = '18242203A';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0207' AND mb.batch_no = 'WTYZ325004';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0208' AND mb.batch_no = 'HSP26006';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0209' AND mb.batch_no = '753022D7';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0210' AND mb.batch_no = '802922D7';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0211' AND mb.batch_no = 'TUR-2269';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0212' AND mb.batch_no = 'VEB26010';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0213' AND mb.batch_no = 'BA26102';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0213' AND mb.batch_no = 'BA26T01';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0214' AND mb.batch_no = 'D45Z007';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0215' AND mb.batch_no = 'NM1735';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0216' AND mb.batch_no = '0062';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0217' AND mb.batch_no = 'GIF036001';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0218' AND mb.batch_no = 'SXF2222A';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0219' AND mb.batch_no = 'CSW1025005E';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0220' AND mb.batch_no = 'DSB1026003A';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0221' AND mb.batch_no = 'FXT0126003AS';
INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date)
SELECT mb.medicine_id, mb.id, 'OPENING_STOCK', mb.quantity_on_hand, 0, mb.quantity_on_hand, 'OPENING_STOCK', mb.id, 'OPENING-STOCK-2026-09-06', TIMESTAMP '2026-09-06 00:00:00', CURRENT_TIMESTAMP
FROM medicine_batches mb
JOIN medicines m ON mb.medicine_id = m.id
WHERE m.code = 'MED-0222' AND mb.batch_no = 'ZVT26121';
