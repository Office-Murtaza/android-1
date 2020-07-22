ALTER TABLE w_coin ADD recall_fee BIGINT(10) DEFAULT NULL AFTER fee;

UPDATE w_coin SET recall_fee = 6 WHERE id = 8;