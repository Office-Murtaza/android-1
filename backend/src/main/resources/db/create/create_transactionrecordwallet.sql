CREATE TABLE IF NOT EXISTS w_transactionrecordwallet (
  id BIGINT(20) NOT NULL AUTO_INCREMENT,
  coin_id BIGINT(20) NOT NULL,
  type INT(2) NOT NULL,
  status INT(2) NOT NULL,
  amount DECIMAL(20, 10) NOT NULL,
  tx_id VARCHAR(255) DEFAULT NULL,
  coinpath_id BIGINT(20) DEFAULT NULL,
  transactionrecordgift_id BIGINT(20) DEFAULT NULL,
  create_date DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  FOREIGN KEY (coin_id) REFERENCES w_coin(id),
  FOREIGN KEY (transactionrecordgift_id) REFERENCES w_transactionrecordgift(id),
  FOREIGN KEY (coinpath_id) REFERENCES w_coinpath(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;