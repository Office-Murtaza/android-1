package com.belco.server.repository;

import com.belco.server.entity.Trade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TradeRep extends JpaRepository<Trade, Long> {

    List<Trade> findAllByStatus(Integer status);
}