DROP TABLE IF EXISTS w_traderequest;
DROP TABLE IF EXISTS w_order;
DROP TABLE IF EXISTS w_trade;

CREATE TABLE IF NOT EXISTS w_trade (
  id BIGINT(20) NOT NULL AUTO_INCREMENT,
  coin_id BIGINT(20) NOT NULL,
  type INT(2) NOT NULL,
  status INT(2) NOT NULL,
  price DECIMAL(20, 10) NOT NULL,
  min_limit DECIMAL(20, 10) NOT NULL,
  max_limit DECIMAL(20, 10) NOT NULL,
  locked_crypto_amount DECIMAL(20, 10) DEFAULT 0,
  payment_methods VARCHAR(100) NOT NULL,
  terms VARCHAR(255) NOT NULL,
  maker_user_id BIGINT(20) NOT NULL,
  create_date DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  FOREIGN KEY (coin_id) REFERENCES w_coin (id),
  FOREIGN KEY (maker_user_id) REFERENCES w_user (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8;

CREATE TABLE IF NOT EXISTS w_order (
  id BIGINT(20) NOT NULL AUTO_INCREMENT,
  coin_id BIGINT(20) NOT NULL,
  status INT(2) NOT NULL,
  price DECIMAL(20, 10) NOT NULL,
  crypto_amount DECIMAL(20, 10) NOT NULL,
  fiat_amount DECIMAL(20, 3) NOT NULL,
  terms VARCHAR(255) NOT NULL,
  trade_id BIGINT(20) DEFAULT NULL,
  maker_user_id BIGINT(20) NOT NULL,
  maker_rate INT(2) DEFAULT NULL,
  taker_user_id BIGINT(20) NOT NULL,
  taker_rate INT(2) DEFAULT NULL,
  create_date DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  FOREIGN KEY (coin_id) REFERENCES w_coin (id),
  FOREIGN KEY (trade_id) REFERENCES w_trade (id),
  FOREIGN KEY (maker_user_id) REFERENCES w_user (id),
  FOREIGN KEY (taker_user_id) REFERENCES w_user (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8;