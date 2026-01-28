ALTER TABLE fee_configurations
    ADD CONSTRAINT uq_fee_config_fee_type_name
        UNIQUE (fee_type, name);
