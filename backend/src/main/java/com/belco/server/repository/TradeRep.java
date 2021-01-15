package com.belco.server.repository;

import com.belco.server.entity.Coin;
import com.belco.server.entity.Identity;
import com.belco.server.entity.Trade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TradeRep extends JpaRepository<Trade, Long> {

    List<Trade> findAllByCoinAndTypeAndIdentityNot(Coin coin, Integer type, Identity identity);

    List<Trade> findAllByCoinAndIdentity(Coin coin, Identity identity);
}