ALTER TABLE w_user DROP COLUMN trade_count;
ALTER TABLE w_user DROP COLUMN trade_rate;

ALTER TABLE w_user ADD COLUMN status INT(2) DEFAULT 1 AFTER password;
ALTER TABLE w_user ADD COLUMN total_trades INT(10) DEFAULT 0 AFTER longitude;
ALTER TABLE w_user ADD COLUMN trading_rate DECIMAL(20, 2) DEFAULT 0 AFTER total_trades;