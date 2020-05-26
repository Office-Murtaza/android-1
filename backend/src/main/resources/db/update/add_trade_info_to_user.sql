ALTER TABLE w_user ADD trade_count BIGINT(10) DEFAULT 0 AFTER longitude;
ALTER TABLE w_user ADD trade_rate DECIMAL(3, 2) DEFAULT NULL AFTER trade_count;