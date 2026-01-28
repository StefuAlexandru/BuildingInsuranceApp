ALTER TABLE fee_configurations
DROP CONSTRAINT IF EXISTS uq_fee_config_fee_type_name;

ALTER TABLE fee_configurations
    ADD CONSTRAINT uq_fee_config_fee_type_name_from
        UNIQUE (fee_type, name, effective_from);
