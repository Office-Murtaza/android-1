DROP TABLE IF EXISTS w_identitykycreview;

CREATE TABLE IF NOT EXISTS w_verificationreview (
  id BIGINT(20) NOT NULL AUTO_INCREMENT,
  tier INT(2) NOT NULL,
  status INT(2) NOT NULL,
  first_name VARCHAR(255) DEFAULT NULL,
  last_name VARCHAR(255) DEFAULT NULL,
  address VARCHAR(255) DEFAULT NULL,
  country VARCHAR(255) DEFAULT NULL,
  province VARCHAR(255) DEFAULT NULL,
  city VARCHAR(255) DEFAULT NULL,
  zip_code VARCHAR(255) DEFAULT NULL,
  id_card_number VARCHAR(255) DEFAULT NULL,
  id_card_number_filename VARCHAR(255) DEFAULT NULL,
  id_card_number_mimetype VARCHAR(255) DEFAULT NULL,
  ssn VARCHAR(255) DEFAULT NULL,
  ssn_filename VARCHAR(255) DEFAULT NULL,
  ssn_mimetype VARCHAR(255) DEFAULT NULL,
  identity_id BIGINT(20) NOT NULL,
  message VARCHAR(255) DEFAULT NULL,
  create_date DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  FOREIGN KEY (identity_id) REFERENCES batm.identity (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8;