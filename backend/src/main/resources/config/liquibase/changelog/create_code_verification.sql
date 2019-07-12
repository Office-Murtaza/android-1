CREATE TABLE `w_code_verification` (
  `user_id` bigint(20) NOT NULL,
  `code` varchar(255) DEFAULT NULL,
  `code_status` varchar(255) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `update_date` datetime DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  CONSTRAINT `FKb4nuwpbnklsxrtvsv6n4oriuo` FOREIGN KEY (`user_id`) REFERENCES `w_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
