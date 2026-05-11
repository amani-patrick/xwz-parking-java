-- XWZ Parking System - Full Database Schema
-- Flyway Migration V1

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Users
CREATE TABLE IF NOT EXISTS users (
    id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    first_name    VARCHAR(100)  NOT NULL,
    last_name     VARCHAR(100)  NOT NULL,
    email         VARCHAR(255)  NOT NULL UNIQUE,
    password      VARCHAR(255)  NOT NULL,
    role          VARCHAR(20)   NOT NULL DEFAULT 'PARKING_TENANT'
                      CHECK (role IN ('ADMIN','PARKING_TENANT')),
    is_active     BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

-- Parkings
CREATE TABLE IF NOT EXISTS parkings (
    id               UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    code             VARCHAR(20)    NOT NULL UNIQUE,
    name             VARCHAR(255)   NOT NULL,
    total_spaces     INT            NOT NULL CHECK (total_spaces > 0),
    available_spaces INT            NOT NULL CHECK (available_spaces >= 0),
    location         VARCHAR(500)   NOT NULL,
    fee_per_hour     NUMERIC(10,2)  NOT NULL CHECK (fee_per_hour >= 0),
    created_by       UUID           REFERENCES users(id),
    is_active        BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

-- Car entries
CREATE TABLE IF NOT EXISTS car_entries (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    plate_number    VARCHAR(20)   NOT NULL,
    parking_code    VARCHAR(20)   NOT NULL REFERENCES parkings(code),
    entry_datetime  TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    exit_datetime   TIMESTAMPTZ   DEFAULT NULL,
    charged_amount  NUMERIC(10,2) NOT NULL DEFAULT 0.00,
    ticket_number   VARCHAR(50)   NOT NULL UNIQUE,
    attendant_id    UUID          REFERENCES users(id),
    status          VARCHAR(20)   NOT NULL DEFAULT 'PARKED'
                        CHECK (status IN ('PARKED','EXITED')),
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_users_email          ON users(email);
CREATE INDEX IF NOT EXISTS idx_parkings_code         ON parkings(code);
CREATE INDEX IF NOT EXISTS idx_entries_plate         ON car_entries(plate_number);
CREATE INDEX IF NOT EXISTS idx_entries_parking       ON car_entries(parking_code);
CREATE INDEX IF NOT EXISTS idx_entries_entry_dt      ON car_entries(entry_datetime);
CREATE INDEX IF NOT EXISTS idx_entries_exit_dt       ON car_entries(exit_datetime);
CREATE INDEX IF NOT EXISTS idx_entries_status        ON car_entries(status);

-- Auto-update updated_at trigger
CREATE OR REPLACE FUNCTION update_updated_at()
RETURNS TRIGGER AS $$
BEGIN NEW.updated_at = NOW(); RETURN NEW; END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_users_updated_at    BEFORE UPDATE ON users        FOR EACH ROW EXECUTE FUNCTION update_updated_at();
CREATE TRIGGER trg_parkings_updated_at BEFORE UPDATE ON parkings      FOR EACH ROW EXECUTE FUNCTION update_updated_at();
CREATE TRIGGER trg_entries_updated_at  BEFORE UPDATE ON car_entries   FOR EACH ROW EXECUTE FUNCTION update_updated_at();

-- Default admin (password: Admin@1234)
INSERT INTO users (first_name, last_name, email, password, role)
VALUES ('System', 'Admin', 'admin@xwzparking.rw',
        '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMlJBU2vMaKl8RXc3zrqQT2hhy', 'ADMIN')
ON CONFLICT (email) DO NOTHING;
