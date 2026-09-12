-- ============================================================
-- Version     : V52
-- Description : Registration validity becomes optional (Master Spec §7.2,
--               §7.4). Until now every facility had to expire its
--               registrations: validity_days was NOT NULL, so the only way
--               to express "our registration never lapses" was to set an
--               absurd number of days and hope nobody noticed the date.
--
--               Hospital/Clinic Admin owns this decision, and both answers
--               are legitimate — a clinic that charges once per patient for
--               life, and one that charges again every year. NULL now means
--               exactly that first answer:
--
--                 validity_days NULL -> registration never expires
--                 validity_days N    -> expires N days after registering
--
--               patient_registrations.expiry_date follows the same rule, so
--               a registration taken while the facility is on "never
--               expires" keeps no expiry date even if the facility later
--               switches to a fixed period — the patient was registered
--               under the terms in force that day, and back-dating an
--               expiry onto them would invent a re-registration fee they
--               were never told about.
--
--               Nothing is backfilled: existing configs keep their day
--               counts and existing registrations keep their dates.
-- Author      : LockDoc App
-- ============================================================

ALTER TABLE registration_fee_configs ALTER COLUMN validity_days SET NULL;

ALTER TABLE patient_registrations ALTER COLUMN expiry_date SET NULL;
