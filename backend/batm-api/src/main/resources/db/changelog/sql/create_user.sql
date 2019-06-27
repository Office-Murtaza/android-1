CREATE TABLE "testdb"."w_user" (
  user_id     INT(10)      NOT NULL AUTO_INCREMENT,
  phone       VARCHAR(20)  NOT NULL,
  password    VARCHAR(100) NOT NULL,
  role        VARCHAR(10)           DEFAULT 'USER',
  create_date DATETIME              DEFAULT CURRENT_TIMESTAMP,
  update_date DATETIME              DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id),
  UNIQUE (phone)
);
