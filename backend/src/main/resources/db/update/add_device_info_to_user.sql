ALTER TABLE w_user ADD COLUMN device_model VARCHAR(50) DEFAULT NULL AFTER platform;
ALTER TABLE w_user ADD COLUMN device_os VARCHAR(50) DEFAULT NULL AFTER device_model;
ALTER TABLE w_user ADD COLUMN app_version VARCHAR(50) DEFAULT NULL AFTER device_os;