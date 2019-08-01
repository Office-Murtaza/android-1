CREATE TABLE `w_unlink` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_date` datetime DEFAULT NULL,
  `update_date` datetime DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_b4iaiijpjr64skcjfrtj855m0` (`user_id`),
  CONSTRAINT `FKkcpvjah2ybaa39p33cr5d88q` FOREIGN KEY (`user_id`) REFERENCES `w_user` (`user_id`)
) ENGINE=InnoDB CHARSET=utf8;
