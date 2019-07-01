CREATE TABLE `w_coins` (
  `coin_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_date` datetime DEFAULT NULL,
  `update_date` datetime DEFAULT NULL,
  `coin_code` varchar(255) DEFAULT NULL,
  `coin_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`coin_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



