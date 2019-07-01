CREATE TABLE `w_refresh_token` (
  `token_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_date` datetime DEFAULT NULL,
  `update_date` datetime DEFAULT NULL,
  `token` varchar(255) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`token_id`),
  KEY `FKt2ubj142plwgifp4qtegv9agj` (`user_id`),
  CONSTRAINT `FKt2ubj142plwgifp4qtegv9agj` FOREIGN KEY (`user_id`) REFERENCES `w_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
