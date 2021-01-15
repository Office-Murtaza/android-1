package com.belco.server.repository;

import com.belco.server.entity.Coin;
import com.belco.server.entity.CoinPath;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CoinPathRep extends JpaRepository<CoinPath, Long> {

    Integer countCoinPathByCoin(Coin coin);

    CoinPath getCoinPathByAddress(String address);

    @Query(value = "SELECT * FROM w_coinpath WHERE coin_id = :coinId AND update_date < NOW() - INTERVAL :hoursAgo HOUR LIMIT 1", nativeQuery = true)
    CoinPath findFirstByCoinIdAndHoursAgo(@Param("coinId") Long coinId, @Param("hoursAgo") Integer hours);
}