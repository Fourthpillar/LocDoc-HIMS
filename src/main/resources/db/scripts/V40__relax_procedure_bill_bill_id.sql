-- ============================================================
-- Version     : V40
-- Description : Bug fix, found by curl-testing V39 the moment it shipped
--               (not a design change) - procedure_bills.bill_id was
--               NOT NULL, but ProcedureBillingService necessarily saves
--               the ProcedureBill row once *before* creating its Bill
--               (the Bill's encounter_id needs the ProcedureBill's own
--               generated id first), then saves it again with bill_id
--               set. Both saves happen inside one @Transactional method
--               so the null is never visible to any other transaction -
--               but Hibernate's own flush of the first save hit the
--               NOT NULL constraint immediately. Relaxed to nullable;
--               the application still guarantees every row that survives
--               its own transaction has a bill_id.
-- Author      : LockDoc App
-- ============================================================

ALTER TABLE procedure_bills ALTER COLUMN bill_id SET NULL;
