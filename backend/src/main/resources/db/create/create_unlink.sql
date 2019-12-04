CREATE TABLE IF NOT EXISTS w_unlink (
  id BIGINT(20) NOT NULL AUTO_INCREMENT,
  user_id BIGINT(20) NOT NULL,
  create_date DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  FOREIGN KEY (user_id) REFERENCES w_user(id)
) ENGINE=InnoDB CHARSET=utf8;