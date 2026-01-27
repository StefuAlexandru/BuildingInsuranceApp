CREATE TABLE fee_configurations (
                                    id BIGSERIAL PRIMARY KEY,
                                    name VARCHAR(120) NOT NULL,
                                    fee_type VARCHAR(32) NOT NULL,
                                    percentage NUMERIC(7,4) NOT NULL CHECK (percentage >= 0),

                                    effective_from DATE NOT NULL,
                                    effective_to DATE,

                                    is_active BOOLEAN NOT NULL DEFAULT TRUE,

                                    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                                    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

                                    CONSTRAINT chk_fee_effective_dates CHECK (effective_to IS NULL OR effective_from <= effective_to)
);

CREATE TRIGGER trg_fee_configurations_updated_at
    BEFORE UPDATE ON fee_configurations
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();

CREATE INDEX idx_fee_cfg_active_dates ON fee_configurations (is_active, effective_from, effective_to);
CREATE INDEX idx_fee_cfg_type ON fee_configurations (fee_type);
