ALTER TABLE w_user ADD COLUMN by_referral_code VARCHAR(10) DEFAULT NULL AFTER notifications_token;