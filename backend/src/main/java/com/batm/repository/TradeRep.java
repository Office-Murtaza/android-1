package com.batm.repository;

import com.batm.entity.Coin;
import com.batm.entity.Identity;
import com.batm.entity.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TradeRep extends JpaRepository<Trade, Long> {

    List<Trade> findAllByCoinAndTypeAndIdentityNot(Coin coin, Integer type, Identity identity);

    List<Trade> findAllByCoinAndIdentity(Coin coin, Identity identity);
}