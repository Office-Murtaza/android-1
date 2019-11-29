package com.batm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.batm.entity.Token;

public interface TokenRep extends JpaRepository<Token, Long> {

    Token findByRefreshToken(String token);

    Token findByAccessToken(String token);

    Token findByUserId(Long userId);
}