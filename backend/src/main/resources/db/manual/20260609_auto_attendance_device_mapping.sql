-- Manual migration reference for beacon-based automatic attendance.
-- This project currently uses Hibernate ddl-auto:update instead of Flyway/Liquibase.

CREATE TABLE IF NOT EXISTS user_devices (
    android_id VARCHAR(64) NOT NULL,
    user_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (android_id),
    CONSTRAINT fk_user_devices_user_id FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX idx_user_devices_user_id ON user_devices (user_id);

CREATE INDEX idx_attendance_auto_open
    ON attendance (user_id, gym_name, check_out_time, check_in_time);
