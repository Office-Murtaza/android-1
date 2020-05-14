INSERT INTO w_coin (code, name, fee, tolerance, scale, profit_c2c)
VALUES ('CATM', 'CATM', NULL, 1, 0, 10);

INSERT INTO w_usercoin (user_id, coin_id, address)
  SELECT user_id, 8, address FROM w_usercoin WHERE coin_id = 3;