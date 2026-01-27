CREATE TABLE brokers (
                         id BIGSERIAL PRIMARY KEY,
                         broker_code VARCHAR(32) NOT NULL,
                         name VARCHAR(120) NOT NULL,
                         email VARCHAR(255) NOT NULL,
                         phone VARCHAR(50) NOT NULL,
                         commission_pct NUMERIC(7,4) CHECK (commission_pct IS NULL OR commission_pct >= 0),

                         created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                         updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

                         CONSTRAINT uq_broker_code UNIQUE (broker_code)
);

CREATE TRIGGER trg_brokers_updated_at
    BEFORE UPDATE ON brokers
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();

CREATE INDEX idx_brokers_code ON brokers (broker_code);
