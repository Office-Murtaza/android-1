ALTER TABLE w_coin ADD order INT(2) DEFAULT NULL AFTER name;

UPDATE w_coin SET order = 1 WHERE id = 1;
UPDATE w_coin SET order = 2 WHERE id = 3;
UPDATE w_coin SET order = 3 WHERE id = 8;
UPDATE w_coin SET order = 4 WHERE id = 2;
UPDATE w_coin SET order = 5 WHERE id = 4;
UPDATE w_coin SET order = 6 WHERE id = 5;
UPDATE w_coin SET order = 7 WHERE id = 6;
UPDATE w_coin SET order = 8 WHERE id = 7;