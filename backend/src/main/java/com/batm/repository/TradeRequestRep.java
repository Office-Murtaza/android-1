package com.batm.repository;

import com.batm.entity.TradeRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TradeRequestRep extends JpaRepository<TradeRequest, Long> {}