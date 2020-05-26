package com.batm.repository;

import com.batm.entity.Coin;
import com.batm.entity.CoinPath;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CoinPathRep extends JpaRepository<CoinPath, Long> {

    Integer countCoinPathByCoin(Coin coin);

    List<CoinPath> findAllByCoin(Coin coin);

    CoinPath getCoinPathByAddress(String address);

    @Query(value = "SELECT * FROM w_coinpath WHERE coin_id = :coinId AND update_date < NOW() - INTERVAL :hoursAgo HOUR LIMIT 1", nativeQuery = true)
    CoinPath findFirstByCoinIdAndHoursAgo(@Param("coinId") Long coinId, @Param("hoursAgo") Integer hours);
}