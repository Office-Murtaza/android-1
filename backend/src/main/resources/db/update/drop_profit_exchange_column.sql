ALTER TABLE w_coin DROP COLUMN profit_exchange;

ALTER TABLE w_transactionrecordwallet CHANGE profit profit_percent DECIMAL(20, 2) DEFAULT NULL;