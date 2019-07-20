CREATE TABLE `w_atm_address` (
  `address_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_date` datetime DEFAULT NULL,
  `update_date` datetime DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `latitude` decimal(19,2) DEFAULT NULL,
  `location_name` varchar(255) DEFAULT NULL,
  `longitude` decimal(19,2) DEFAULT NULL,
  PRIMARY KEY (`address_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
