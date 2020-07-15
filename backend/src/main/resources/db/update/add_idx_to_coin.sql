ALTER TABLE w_coin ADD idx INT(2) DEFAULT 0 AFTER name;

UPDATE w_coin SET idx = 1 WHERE id = 1;
UPDATE w_coin SET idx = 2 WHERE id = 3;
UPDATE w_coin SET idx = 3 WHERE id = 8;
UPDATE w_coin SET idx = 4 WHERE id = 2;
UPDATE w_coin SET idx = 5 WHERE id = 4;
UPDATE w_coin SET idx = 6 WHERE id = 5;
UPDATE w_coin SET idx = 7 WHERE id = 6;
UPDATE w_coin SET idx = 8 WHERE id = 7;