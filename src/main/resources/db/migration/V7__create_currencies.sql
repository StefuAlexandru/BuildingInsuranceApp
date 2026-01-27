CREATE TABLE currencies (
                            id BIGSERIAL PRIMARY KEY,
                            code VARCHAR(32) NOT NULL,
                            name VARCHAR(32) NOT NULL,
                            exchange_rate_to_base NUMERIC(12,6) NOT NULL CHECK (exchange_rate_to_base > 0),
                            is_active BOOLEAN NOT NULL DEFAULT TRUE,

                            created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                            updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

                            CONSTRAINT uq_currency_code UNIQUE (code)
);

CREATE TRIGGER trg_currencies_updated_at
    BEFORE UPDATE ON currencies
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();

CREATE INDEX idx_currencies_code ON currencies (code);
CREATE INDEX idx_currencies_active ON currencies (is_active);
