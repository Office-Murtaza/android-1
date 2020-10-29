INSERT INTO w_coin(code, name, idx, tolerance, scale, profit_exchange) VALUES ('USDT', 'Tether', 2, 0.1, 2, 10);
INSERT INTO w_coin(code, name, idx, tolerance, scale, profit_exchange) VALUES ('DASH', 'Dash', 10, 0.01, 2, 10);
INSERT INTO w_coin(code, name, idx, tolerance, scale, profit_exchange) VALUES ('DOGE', 'Dogecoin', 11, 1, 0, 10);

UPDATE w_coin SET idx = 3 WHERE id = 3;
UPDATE w_coin SET idx = 4 WHERE id = 8;
UPDATE w_coin SET idx = 5 WHERE id = 2;
UPDATE w_coin SET idx = 6 WHERE id = 4;
UPDATE w_coin SET idx = 7 WHERE id = 5;
UPDATE w_coin SET idx = 8 WHERE id = 6;
UPDATE w_coin SET idx = 9 WHERE id = 7;