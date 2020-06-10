DROP TABLE IF EXISTS w_traderequest;

CREATE TABLE IF NOT EXISTS w_traderequest (
  id BIGINT(20) NOT NULL AUTO_INCREMENT,
  coin_id BIGINT(20) NOT NULL,
  status INT(2) NOT NULL,
  payment_method VARCHAR(100) NOT NULL,
  price DECIMAL(20, 3) NOT NULL,
  crypto_amount DECIMAL(20, 10) NOT NULL,
  fiat_amount DECIMAL(20, 3) NOT NULL,
  terms VARCHAR(255) NOT NULL,
  details VARCHAR(255) NOT NULL,
  buy_identity_id BIGINT(20) NOT NULL,
  sell_identity_id BIGINT(20) NOT NULL,
  buy_rate DECIMAL(3, 2) DEFAULT NULL,
  sell_rate DECIMAL(3, 2) DEFAULT NULL,
  create_date DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  FOREIGN KEY (coin_id) REFERENCES w_coin(id),
  FOREIGN KEY (buy_identity_id) REFERENCES identity(id),
  FOREIGN KEY (sell_identity_id) REFERENCES identity(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;