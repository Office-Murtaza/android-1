ALTER TABLE w_user CHANGE app_token notifications_token VARCHAR(100) DEFAULT NULL;
ALTER TABLE w_user CHANGE receive_push_notifications notifications_enabled BOOL DEFAULT TRUE;