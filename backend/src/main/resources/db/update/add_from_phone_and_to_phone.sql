ALTER TABLE w_transactionrecordwallet ADD from_phone VARCHAR(50) DEFAULT NULL AFTER phone;
ALTER TABLE w_transactionrecordwallet ADD to_phone VARCHAR(50) DEFAULT NULL AFTER from_phone;

ALTER TABLE w_transactionrecordwallet DROP COLUMN phone;

TRUNCATE TABLE w_transactionrecordwallet;