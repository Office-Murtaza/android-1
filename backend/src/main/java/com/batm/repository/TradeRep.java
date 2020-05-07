package com.batm.repository;

import com.batm.entity.Trade;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TradeRep extends JpaRepository<Trade, Long> {

    Integer countTradeByType(Integer type);

    List<Trade> findByTypeOrderByMarginAsc(Integer type, Pageable page);

    List<Trade> findByTypeOrderByMarginDesc(Integer type, Pageable page);
}