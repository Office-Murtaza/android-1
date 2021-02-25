DELETE FROM w_transactionrecordwallet;

ALTER TABLE w_transactionrecordwallet DROP FOREIGN KEY w_transactionrecordwallet_ibfk_2;

ALTER TABLE w_transactionrecordwallet DROP COLUMN identity_id;

ALTER TABLE w_transactionrecordwallet ADD COLUMN identity_id BIGINT(20) DEFAULT NULL AFTER amount;

ALTER TABLE w_transactionrecordwallet ADD FOREIGN KEY (identity_id) REFERENCES identity(id);