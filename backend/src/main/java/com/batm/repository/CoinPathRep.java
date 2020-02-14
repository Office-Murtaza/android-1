package com.batm.repository;

import com.batm.entity.Coin;
import com.batm.entity.CoinPath;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoinPathRep extends JpaRepository<CoinPath, Long> {

    Integer countCoinPathByCoin(Coin coin);
}