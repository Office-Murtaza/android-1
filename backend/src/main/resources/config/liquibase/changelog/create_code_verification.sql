CREATE TABLE `w_code_verification` (
  `code_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_date` datetime DEFAULT NULL,
  `update_date` datetime DEFAULT NULL,
  `code` varchar(255) DEFAULT NULL,
  `code_status` varchar(255) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`code_id`),
  KEY `FKb4nuwpbnklsxrtvsv6n4oriuo` (`user_id`),
  CONSTRAINT `FKb4nuwpbnklsxrtvsv6n4oriuo` FOREIGN KEY (`user_id`) REFERENCES `w_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
