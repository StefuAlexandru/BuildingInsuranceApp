ALTER TABLE policies
    ADD COLUMN broker_commission_pct NUMERIC(7,4),
    ADD COLUMN broker_commission_amount NUMERIC(14,2);
