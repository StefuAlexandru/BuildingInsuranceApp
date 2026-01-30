DROP INDEX IF EXISTS idx_fee_cfg_active_dates;
DROP INDEX IF EXISTS idx_risk_cfg_active_level_ref;
DROP INDEX IF EXISTS idx_risk_cfg_active_level_building_type;

ALTER TABLE fee_configurations DROP COLUMN is_active;
ALTER TABLE risk_factor_configurations DROP COLUMN is_active;

CREATE INDEX IF NOT EXISTS idx_fee_cfg_dates
    ON fee_configurations (effective_from, effective_to);

CREATE INDEX IF NOT EXISTS idx_risk_cfg_level_ref_dates
    ON risk_factor_configurations (level, reference_id, effective_from, effective_to);

CREATE INDEX IF NOT EXISTS idx_risk_cfg_level_building_type_dates
    ON risk_factor_configurations (level, building_type, effective_from, effective_to);