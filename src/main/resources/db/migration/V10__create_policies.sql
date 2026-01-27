CREATE TABLE policies (
                          id BIGSERIAL PRIMARY KEY,
                          policy_number VARCHAR(40) NOT NULL,
                          status VARCHAR(16) NOT NULL CHECK (status IN ('DRAFT','ACTIVE','CANCELLED','EXPIRED')),

                          client_id BIGINT NOT NULL REFERENCES clients(id) ON DELETE RESTRICT,
                          building_id BIGINT NOT NULL REFERENCES buildings(id) ON DELETE RESTRICT,
                          broker_id BIGINT NOT NULL REFERENCES brokers(id) ON DELETE RESTRICT,
                          currency_id BIGINT NOT NULL REFERENCES currencies(id) ON DELETE RESTRICT,

                          start_date DATE NOT NULL,
                          end_date DATE NOT NULL,

                          base_premium_amount NUMERIC(14,2) NOT NULL CHECK (base_premium_amount > 0),
                          final_premium_amount NUMERIC(14,2) NOT NULL CHECK (final_premium_amount > 0),

                          activated_at TIMESTAMPTZ,
                          cancelled_at TIMESTAMPTZ,
                          cancellation_reason VARCHAR(255),

                          created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                          updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

                          CONSTRAINT uq_policy_number UNIQUE (policy_number),
                          CONSTRAINT chk_policy_dates CHECK (start_date < end_date),
                          CONSTRAINT chk_policy_cancel_fields CHECK (
                              (status <> 'CANCELLED')
                                  OR
                              (cancelled_at IS NOT NULL AND cancellation_reason IS NOT NULL)
                              )
);

CREATE TRIGGER trg_policies_updated_at
    BEFORE UPDATE ON policies
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();

CREATE INDEX idx_policy_broker_status ON policies (broker_id, status);
CREATE INDEX idx_policy_client_id ON policies (client_id);
CREATE INDEX idx_policy_dates ON policies (start_date, end_date);
