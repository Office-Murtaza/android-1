ALTER TABLE w_user ADD COLUMN app_token VARCHAR(100) DEFAULT NULL AFTER platform;
ALTER TABLE w_user ADD COLUMN receive_push_notifications BOOL DEFAULT TRUE AFTER app_token;