ALTER TABLE w_coin ADD gas_limit BIGINT(10) DEFAULT NULL AFTER fee;
ALTER TABLE w_coin ADD gas_price BIGINT(10) DEFAULT NULL AFTER gas_limit;