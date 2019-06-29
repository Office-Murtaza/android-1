package com.batm.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.batm.entity.CodeVerification;
import com.batm.entity.UserCoin;

public interface UserCoinRepository extends JpaRepository<UserCoin, Long> {

	CodeVerification findByUserUserId(Long userId);

}
