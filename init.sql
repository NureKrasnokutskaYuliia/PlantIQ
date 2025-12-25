/*DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS devices CASCADE;
DROP TABLE IF EXISTS plants CASCADE;
DROP TABLE IF EXISTS sensor_data CASCADE;
DROP TABLE IF EXISTS watering_schedules CASCADE;
DROP TABLE IF EXISTS watering_events CASCADE;
DROP TABLE IF EXISTS notifications CASCADE;
DROP TABLE IF EXISTS system_settings CASCADE;*/

CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'owner' 
        CHECK (role IN ('owner', 'admin')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    last_login TIMESTAMP WITH TIME ZONE
);

CREATE TABLE devices (
    device_id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    model VARCHAR(100),
    status VARCHAR(50) NOT NULL DEFAULT 'offline' 
        CHECK (status IN ('online', 'offline', 'error', 'maintenance')),
    firmware_version VARCHAR(50),
    last_sync TIMESTAMP WITH TIME ZONE,
    serial_number VARCHAR(100) UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE plants (
    plant_id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    device_id INTEGER REFERENCES devices(device_id) ON DELETE SET NULL,
    name VARCHAR(100) NOT NULL,
    species VARCHAR(100),
    optimal_moisture_min DECIMAL(5,2) CHECK (optimal_moisture_min >= 0 AND optimal_moisture_min <= 100),
    optimal_moisture_max DECIMAL(5,2) CHECK (optimal_moisture_max >= 0 AND optimal_moisture_max <= 100),
    optimal_light_min DECIMAL(10,2) CHECK (optimal_light_min >= 0),
    optimal_light_max DECIMAL(10,2) CHECK (optimal_light_max >= 0),
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    CONSTRAINT chk_moisture_range CHECK (optimal_moisture_min <= optimal_moisture_max),
    CONSTRAINT chk_light_range CHECK (optimal_light_min <= optimal_light_max)
);

CREATE TABLE sensor_data (
    data_id SERIAL PRIMARY KEY,
    plant_id INTEGER NOT NULL REFERENCES plants(plant_id) ON DELETE CASCADE,
    device_id INTEGER NOT NULL REFERENCES devices(device_id) ON DELETE CASCADE,
    timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    soil_moisture DECIMAL(5,2) CHECK (soil_moisture >= 0 AND soil_moisture <= 100),
    light_intensity DECIMAL(10,2) CHECK (light_intensity >= 0),
    battery_level DECIMAL(5,2) CHECK (battery_level >= 0 AND battery_level <= 100)
);

CREATE TABLE watering_schedules (
    schedule_id SERIAL PRIMARY KEY,
    plant_id INTEGER NOT NULL REFERENCES plants(plant_id) ON DELETE CASCADE,
    start_time TIME NOT NULL,
    interval_hours INTEGER NOT NULL CHECK (interval_hours > 0),
    amount_ml INTEGER NOT NULL CHECK (amount_ml > 0),
    enabled BOOLEAN DEFAULT TRUE,
    days_of_week INTEGER[] DEFAULT '{0,1,2,3,4,5,6}',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE watering_events (
    event_id SERIAL PRIMARY KEY,
    plant_id INTEGER NOT NULL REFERENCES plants(plant_id) ON DELETE CASCADE,
    schedule_id INTEGER REFERENCES watering_schedules(schedule_id) ON DELETE SET NULL,
    timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    amount_ml INTEGER NOT NULL CHECK (amount_ml > 0),
    mode VARCHAR(50) NOT NULL DEFAULT 'automatic' 
        CHECK (mode IN ('automatic', 'manual', 'scheduled')),
    duration_seconds INTEGER,
    status VARCHAR(50) DEFAULT 'completed' 
        CHECK (status IN ('completed', 'failed', 'cancelled')),
    notes TEXT
);

CREATE TABLE notifications (
    notification_id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    plant_id INTEGER REFERENCES plants(plant_id) ON DELETE CASCADE,
    device_id INTEGER REFERENCES devices(device_id) ON DELETE CASCADE,
    timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    type VARCHAR(50) NOT NULL 
        CHECK (type IN ('low_moisture', 'low_light', 'high_light', 'device_offline', 
                        'watering_complete', 'watering_failed', 'battery_low', 'system')),
    message TEXT NOT NULL,
    read BOOLEAN DEFAULT FALSE,
    priority VARCHAR(20) DEFAULT 'normal' 
        CHECK (priority IN ('low', 'normal', 'high', 'critical')),
    read_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE system_settings (
    setting_id SERIAL PRIMARY KEY,
    setting_key VARCHAR(100) NOT NULL UNIQUE,
    setting_value TEXT,
    description TEXT,
    updated_by INTEGER REFERENCES users(user_id) ON DELETE SET NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_devices_updated_at BEFORE UPDATE ON devices
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_plants_updated_at BEFORE UPDATE ON plants
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_watering_schedules_updated_at BEFORE UPDATE ON watering_schedules
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_system_settings_updated_at BEFORE UPDATE ON system_settings
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
