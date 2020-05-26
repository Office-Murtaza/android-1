package com.batm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.batm.entity.Coin;

public interface CoinRep extends JpaRepository<Coin, Long> {

    Coin findCoinByCode(String code);
}