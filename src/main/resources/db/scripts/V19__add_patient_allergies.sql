-- ============================================================
-- Version     : V19
-- Description : Step 5 - adds allergies to the existing shared Patient
--               table (Master Spec §6/§7.4/§17.6's PatientHeaderBar
--               "allergy banner", and the dispensing safety checks
--               §11.5 will read the same column). Patient itself is NOT
--               otherwise restructured in this pass - it already
--               correctly serves both OP and Pharmacy per its own class
--               comment, satisfying §6 invariant 7. The rest of the real
--               OP card's demographic fields (title, blood group,
--               religion, marital status, education, occupation,
--               identification mark, photograph, ID proof, referral
--               source, ABHA ID) are deliberately deferred - they don't
--               block registration/visit/billing from working, the same
--               "can trail behind" allowance §16 step 5 itself grants
--               to OP Reports/packages/MLC.
-- Author      : LockDoc App
-- ============================================================

ALTER TABLE patients ADD COLUMN IF NOT EXISTS allergies VARCHAR(500);
