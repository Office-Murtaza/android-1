package com.belco.server.repository;

import com.belco.server.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenRep extends JpaRepository<Token, Long> {

    Token findByRefreshToken(String token);

    Token findByAccessToken(String token);

    Token findByUserId(Long userId);
}