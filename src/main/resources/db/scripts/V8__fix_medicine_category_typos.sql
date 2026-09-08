-- ============================================================
-- Version     : V8
-- Description : Correct data-entry typos transcribed verbatim
--               from the source "Medicine Stock Report" into the
--               V5 seed data - confirmed unambiguous misspellings,
--               not judgment calls:
--                 - 'OINTEMENT' -> 'OINTMENT'
--                 - 'SWEB'      -> 'SWAB'
--                 - 'GAUGE' was used for both actual needle/
--                   cannula gauge sizes (correct - see
--                   'GAUGE S.D 18 M', left unchanged) and for
--                   bandage/dressing items (incorrect - those are
--                   corrected to 'GAUZE').
--               The medicine name 'STERILE GAUGE SWAB' carries
--               the same typo and is corrected to
--               'STERILE GAUZE SWAB'.
-- Author      : LockDoc App
-- ============================================================

UPDATE medicines SET category = 'OINTMENT' WHERE category = 'OINTEMENT';

UPDATE medicines SET category = 'SWAB' WHERE category = 'SWEB';

UPDATE medicines SET category = 'GAUZE'
    WHERE category = 'GAUGE' AND name IN ('BANDAGE 4 INCH', 'BANDAGE 6 INCH', 'GAUZE PADS');

UPDATE medicines SET name = 'STERILE GAUZE SWAB' WHERE code = 'MED-0173';
