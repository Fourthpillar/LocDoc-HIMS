-- Cancel Consultation (Master Spec §5 principle 4, §7.5) - two real gaps found live:
-- (1) an OP visit itself had no cancel path at all, only its Appointment/Bill entities did;
-- (2) cancelling a paid bill never refunded anything anywhere in this app - it just flipped
-- status to CANCELLED and left the money "collected" in every day-collection total forever.
ALTER TABLE op_visits ADD COLUMN cancel_reason VARCHAR(300);

-- Orthogonal to bills.status (which stays CANCELLED, unchanged) - a cancelled bill that had
-- money paid against it needs an explicit, reception-initiated refund action (their own
-- deliberate step recording how the money physically went back), not a silent auto-refund
-- the moment an approval fires. NONE = nothing was ever paid, or already settled; PENDING =
-- cancelled with money owed back; REFUNDED = reception has recorded the refund payment.
ALTER TABLE bills ADD COLUMN refund_status VARCHAR(20) NOT NULL DEFAULT 'NONE';
