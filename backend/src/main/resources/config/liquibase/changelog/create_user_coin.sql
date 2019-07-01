CREATE TABLE `w_user_coin_map` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_date` datetime DEFAULT NULL,
  `update_date` datetime DEFAULT NULL,
  `coin_code` varchar(255) DEFAULT NULL,
  `public_key` varchar(255) DEFAULT NULL,
  `coin_id` bigint(20) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK6lnkikjwxwbvagvv320iqyud8` (`coin_id`),
  KEY `FKs7conr0n2v88ryhj10o4l6c0u` (`user_id`),
  CONSTRAINT `FK6lnkikjwxwbvagvv320iqyud8` FOREIGN KEY (`coin_id`) REFERENCES `w_coins` (`coin_id`),
  CONSTRAINT `FKs7conr0n2v88ryhj10o4l6c0u` FOREIGN KEY (`user_id`) REFERENCES `w_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
