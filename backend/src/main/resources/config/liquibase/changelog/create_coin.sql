CREATE TABLE `w_coins` (
  `coin_id` varchar(255) NOT NULL,
  `coin_name` varchar(255) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `update_date` datetime DEFAULT NULL,
  PRIMARY KEY (`coin_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
