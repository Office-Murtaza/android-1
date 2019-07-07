package com.batm.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.batm.entity.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

	RefreshToken findByTokenAndUserUserId(String token, Long userId);
	
	RefreshToken findByUserUserId(Long userId);

}
