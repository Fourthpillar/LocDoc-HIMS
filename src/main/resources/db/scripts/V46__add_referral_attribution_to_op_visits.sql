-- ============================================================
-- Version     : V46
-- Description : Referral doctor / PRO attribution on OP visits (Master
--               Spec §7.4 - "referral doctor + PRO" is a captured
--               registration/visit field; §8.1 - "LocDoc-HIMS reports
--               consultation counts and computed shares - it does not
--               transfer money"). Needed for the Commission report
--               (§17.7 #12a) - OpVisit previously carried neither
--               reference, only the unlinked ReferralDoctor/Pro masters
--               (V37) existed with nothing pointing at them.
--
--               Commission itself is still computed against
--               commission_basis.party_name as a free-text match (that
--               table's own pre-existing design, V35/CommissionBasis's
--               own javadoc - it predates the ReferralDoctor/Pro master
--               tables and was never retrofitted to reference them by
--               id) - not changed here, to keep this migration to
--               exactly the one new gap it closes.
-- Author      : LockDoc App
-- ============================================================

ALTER TABLE op_visits ADD COLUMN IF NOT EXISTS referral_doctor_id BIGINT;
ALTER TABLE op_visits ADD COLUMN IF NOT EXISTS pro_id BIGINT;

ALTER TABLE op_visits ADD CONSTRAINT IF NOT EXISTS fk_op_visit_referral_doctor FOREIGN KEY (referral_doctor_id) REFERENCES referral_doctors (id);
ALTER TABLE op_visits ADD CONSTRAINT IF NOT EXISTS fk_op_visit_pro FOREIGN KEY (pro_id) REFERENCES pros (id);
