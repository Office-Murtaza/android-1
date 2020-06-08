package com.batm.repository;

import com.batm.entity.Coin;
import com.batm.entity.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TradeRep extends JpaRepository<Trade, Long> {

    List<Trade> findAllByCoin(Coin coin);
}