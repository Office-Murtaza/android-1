package com.belco.server.repository;

import com.belco.server.entity.Coin;
import com.belco.server.entity.Identity;
import com.belco.server.entity.TradeRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TradeRequestRep extends JpaRepository<TradeRequest, Long> {

    Integer countAllByCoinAndBuyIdentityOrSellIdentity(Coin coin, Identity buyIdentity, Identity sellIdentity);

    List<TradeRequest> findAllByCoinAndBuyIdentityOrSellIdentityOrderByCreateDateDesc(Coin coin, Identity buyIdentity, Identity sellIdentity, Pageable page);
}