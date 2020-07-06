DROP TABLE IF EXISTS w_transactionrecordwallet;
DROP TABLE IF EXISTS w_transactionrecordc2c;
DROP TABLE IF EXISTS w_transactionrecordgift;
DROP TABLE IF EXISTS w_transactionrecordreserve;


CREATE TABLE IF NOT EXISTS w_transactionrecordwallet (
  id BIGINT(20) NOT NULL AUTO_INCREMENT,
  coin_id BIGINT(20) NOT NULL,
  type INT(2) NOT NULL,
  status INT(2) NOT NULL,
  amount DECIMAL(20, 10) NOT NULL,
  identity_id BIGINT(20) NOT NULL,
  receiver_status INT(2) DEFAULT NULL,
  phone VARCHAR(50) DEFAULT NULL,
  image_id VARCHAR(255) DEFAULT NULL,
  message VARCHAR(100) DEFAULT NULL,
  profit DECIMAL(20, 10) DEFAULT NULL,
  coinpath_id BIGINT(20) DEFAULT NULL,
  tx_id VARCHAR(255) NOT NULL,
  ref_coin_id BIGINT(20) DEFAULT NULL,
  ref_amount DECIMAL(20, 10) DEFAULT NULL,
  ref_tx_id VARCHAR(255) DEFAULT NULL,
  create_date DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  FOREIGN KEY (coin_id) REFERENCES w_coin(id),
  FOREIGN KEY (identity_id) REFERENCES identity(id),
  FOREIGN KEY (coinpath_id) REFERENCES w_coinpath(id),
  FOREIGN KEY (ref_coin_id) REFERENCES w_coin(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;