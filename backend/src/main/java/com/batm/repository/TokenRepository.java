package com.batm.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.batm.entity.Token;

public interface TokenRepository extends JpaRepository<Token, Long> {
	
	Token findByRefreshToken(String token);
	
	Token findByAccessToken(String token);
	
	Token findByUserUserId(Long userId);

}
