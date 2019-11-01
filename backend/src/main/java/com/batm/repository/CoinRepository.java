package com.batm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.batm.entity.Coin;

public interface CoinRepository extends JpaRepository<Coin, Long> {}