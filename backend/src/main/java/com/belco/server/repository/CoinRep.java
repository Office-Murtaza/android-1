package com.belco.server.repository;

import com.belco.server.entity.Coin;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CoinRep extends JpaRepository<Coin, Long> {

    List<Coin> findAllByOrderByIdxAsc();
}