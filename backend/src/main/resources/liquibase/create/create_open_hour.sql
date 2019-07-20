CREATE TABLE `w_open_hour` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_date` datetime DEFAULT NULL,
  `update_date` datetime DEFAULT NULL,
  `days` varchar(255) DEFAULT NULL,
  `hours` varchar(255) DEFAULT NULL,
  `address_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK9pjlngoqck0avdrq4hjoo0pcg` (`address_id`),
  CONSTRAINT `FK9pjlngoqck0avdrq4hjoo0pcg` FOREIGN KEY (`address_id`) REFERENCES `w_atm_address` (`address_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
