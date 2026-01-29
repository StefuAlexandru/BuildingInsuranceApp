ALTER TABLE risk_factor_configurations
    ADD CONSTRAINT uq_level_ref_building_type_effective_from
        UNIQUE (level, reference_id, building_type, effective_from)