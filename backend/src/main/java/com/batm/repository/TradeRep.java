package com.batm.repository;

import com.batm.entity.Coin;
import com.batm.entity.Trade;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TradeRep extends JpaRepository<Trade, Long> {

    Integer countTradeByCoinAndType(Coin coin, Integer type);

    List<Trade> findAllByCoinAndTypeOrderByMarginAsc(Coin coin, Integer type, Pageable page);

    List<Trade> findAllByCoinAndTypeOrderByMarginDesc(Coin coin, Integer type, Pageable page);

    List<Trade> findAllByCoinAndType(Coin coin, Integer type);
}