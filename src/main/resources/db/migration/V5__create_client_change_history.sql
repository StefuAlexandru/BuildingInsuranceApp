CREATE TABLE client_change_history (
                                       id          BIGSERIAL PRIMARY KEY,
                                       client_id   BIGINT       NOT NULL,
                                       field       VARCHAR(40)  NOT NULL CHECK (field IN ('IDENTIFICATION_NUMBER','CLIENT_TYPE')),
                                       old_value   VARCHAR(255) NOT NULL,
                                       new_value   VARCHAR(255) NOT NULL,
                                       reason      VARCHAR(500) NOT NULL,
                                       changed_by  VARCHAR(255) NOT NULL,
                                       changed_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),

                                       CONSTRAINT fk_cch_client
                                           FOREIGN KEY (client_id)
                                               REFERENCES clients(id)
                                               ON DELETE CASCADE
);

CREATE INDEX idx_client_change_history_client_id ON client_change_history(client_id, changed_at DESC);
CREATE INDEX idx_client_change_history_changed_at ON client_change_history(changed_at);

ALTER TABLE buildings
    ADD CONSTRAINT fk_buildings_client
        FOREIGN KEY (client_id) REFERENCES clients(id)
            ON DELETE RESTRICT;

