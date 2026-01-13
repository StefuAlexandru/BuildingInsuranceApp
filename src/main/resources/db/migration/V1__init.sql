CREATE TABLE countries (
                           id BIGSERIAL PRIMARY KEY,
                           name VARCHAR(120) NOT NULL UNIQUE
);

CREATE TABLE counties (
                          id BIGSERIAL PRIMARY KEY,
                          country_id BIGINT NOT NULL REFERENCES countries(id) ON DELETE RESTRICT,
                          name VARCHAR(120) NOT NULL,
                          CONSTRAINT uq_county_country_name UNIQUE (country_id, name)
);

CREATE TABLE cities (
                        id BIGSERIAL PRIMARY KEY,
                        county_id BIGINT NOT NULL REFERENCES counties(id) ON DELETE RESTRICT,
                        name VARCHAR(120) NOT NULL,
                        CONSTRAINT uq_city_county_name UNIQUE (county_id, name)
);

CREATE TABLE clients (
                         id BIGSERIAL PRIMARY KEY,
                         client_type VARCHAR(20) NOT NULL CHECK (client_type IN ('INDIVIDUAL', 'COMPANY')),
                         name VARCHAR(255) NOT NULL,
                         identification_number VARCHAR(32) NOT NULL UNIQUE,
                         email VARCHAR(255),
                         phone VARCHAR(50),

                         address_street VARCHAR(255),
                         address_number VARCHAR(50),
                         address_city_id BIGINT REFERENCES cities(id) ON DELETE SET NULL,

                         created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                         updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_clients_name ON clients (name);
CREATE INDEX idx_clients_identification_number ON clients (identification_number);

CREATE TABLE buildings (
                           id BIGSERIAL PRIMARY KEY,
                           client_id BIGINT NOT NULL REFERENCES clients(id) ON DELETE RESTRICT,
                           city_id BIGINT NOT NULL REFERENCES cities(id) ON DELETE RESTRICT,

                           street VARCHAR(255) NOT NULL,
                           number VARCHAR(50) NOT NULL,

                           construction_year INT NOT NULL
                               CHECK (construction_year BETWEEN 1800 AND EXTRACT(YEAR FROM now())::INT),

  building_type VARCHAR(30) NOT NULL,
  floors INT CHECK (floors IS NULL OR floors >= 0),

  surface_area NUMERIC(12,2) NOT NULL CHECK (surface_area > 0),
  insured_value NUMERIC(14,2) NOT NULL CHECK (insured_value > 0),

  flood_zone BOOLEAN,
  earthquake_zone BOOLEAN,

  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_buildings_client_id ON buildings (client_id);
CREATE INDEX idx_buildings_city_id ON buildings (city_id);
