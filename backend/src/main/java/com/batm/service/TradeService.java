package com.batm.service;

import com.batm.dto.*;
import com.batm.entity.*;
import com.batm.model.TradeSort;
import com.batm.model.TradeStatus;
import com.batm.model.TradeType;
import com.batm.repository.TradeRep;
import com.batm.repository.TradeRequestRep;
import com.batm.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class TradeService {

    @Autowired
    private TradeRep tradeRep;

    @Autowired
    private TradeRequestRep tradeRequestRep;

    @Autowired
    private UserService userService;

    public Long createTrade(Long userId, CoinService.CoinEnum coinCode, TradeDTO dto) {
        try {
            Trade trade = new Trade();
            trade.setIdentity(userService.findById(userId).getIdentity());
            trade.setCoin(coinCode.getCoinEntity());
            trade.setType(dto.getType());
            trade.setPaymentMethod(dto.getPaymentMethod());
            trade.setMargin(dto.getMargin());
            trade.setMinLimit(dto.getMinLimit());
            trade.setMaxLimit(dto.getMaxLimit());
            trade.setTerms(dto.getTerms());

            return tradeRep.save(trade).getId();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean updateTrade(TradeDTO dto) {
        try {
            Trade trade = tradeRep.findById(dto.getId()).get();

            trade.setType(dto.getType());
            trade.setPaymentMethod(dto.getPaymentMethod());
            trade.setMargin(dto.getMargin());
            trade.setMinLimit(dto.getMinLimit());
            trade.setMaxLimit(dto.getMaxLimit());
            trade.setTerms(dto.getTerms());

            tradeRep.save(trade);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean deleteTrade(Long id) {
        tradeRep.deleteById(id);

        return true;
    }

    public TradeListDTO getTrades(Long userId, CoinService.CoinEnum coinCode, Integer index, Integer sort) {
        User user = userService.findById(userId);
        Identity identity = user.getIdentity();
        Coin coin = coinCode.getCoinEntity();
        BigDecimal price = coinCode.getPrice();
        List<Trade> trades = tradeRep.findAllByCoin(coin);

        List<TradeDetailsDTO> buyDTOs = new ArrayList<>();
        List<TradeDetailsDTO> sellDTOs = new ArrayList<>();
        List<TradeDetailsDTO> myDTOs = new ArrayList<>();

        sort = user.getLatitude() == null ? TradeSort.PRICE.getValue() : sort;

        for (Trade tr : trades) {
            tr.setPrice(price
                    .multiply(tr.getMargin().divide(new BigDecimal(100)).add(BigDecimal.ONE))
                    .setScale(3, RoundingMode.DOWN).stripTrailingZeros());

            if (tr.getIdentity().getId() == identity.getId()) {
                myDTOs.add(tr.toDTO());
            } else if (tr.getType() == TradeType.BUY.getValue()) {
                if (sort == TradeSort.DISTANCE.getValue()) {
                    tr.setDistance(Util.distance(user, tr.getIdentity().getUser()));
                }

                buyDTOs.add(tr.toDTO());
            } else if (tr.getType() == TradeType.SELL.getValue()) {
                if (sort == TradeSort.DISTANCE.getValue()) {
                    tr.setDistance(Util.distance(user, tr.getIdentity().getUser()));
                }

                sellDTOs.add(tr.toDTO());
            }
        }

        TradeListDTO dto = new TradeListDTO();
        dto.setBuyTotal(buyDTOs.size());
        dto.setSellTotal(sellDTOs.size());
        dto.setMyTotal(myDTOs.size());

        if (sort == TradeSort.PRICE.getValue()) {
            buyDTOs.sort(Comparator.comparing(TradeDetailsDTO::getPrice));
            sellDTOs.sort(Comparator.comparing(TradeDetailsDTO::getPrice).reversed());
        } else if (sort == TradeSort.DISTANCE.getValue()) {
            buyDTOs.sort(Comparator.comparing(TradeDetailsDTO::getDistance));
            sellDTOs.sort(Comparator.comparing(TradeDetailsDTO::getDistance));
        }

        dto.setBuyTrades(buyDTOs.subList(index - 1, Math.min(buyDTOs.size(), index + 10 - 1)));
        dto.setSellTrades(sellDTOs.subList(index - 1, Math.min(sellDTOs.size(), index + 10 - 1)));
        dto.setMyTrades(myDTOs.subList(index - 1, Math.min(myDTOs.size(), index + 10 - 1)));

        return dto;
    }

    public Long createTradeRequest(Long userId, CoinService.CoinEnum coinCode, TradeRequestDTO dto) {
        try {
            Trade trade = tradeRep.findById(dto.getTradeId()).get();
            Identity tradeIdentity = trade.getIdentity();
            Identity requestIdentity = userService.findByUserId(userId);

            TradeRequest tr = new TradeRequest();
            tr.setTrade(trade);
            tr.setStatus(TradeStatus.CREATED.getValue());
            tr.setCoin(coinCode.getCoinEntity());
            tr.setPaymentMethod(trade.getPaymentMethod());
            tr.setMargin(trade.getMargin());
            tr.setPrice(dto.getPrice());
            tr.setCryptoAmount(dto.getCryptoAmount());
            tr.setFiatAmount(dto.getFiatAmount());
            tr.setMinLimit(trade.getMinLimit());
            tr.setMaxLimit(trade.getMaxLimit());
            tr.setTerms(tr.getTerms());
            tr.setDetails(dto.getDetails());

            if (trade.getType() == TradeType.BUY.getValue()) {
                tr.setBuyIdentity(tradeIdentity);
                tr.setSellIdentity(requestIdentity);
            } else {
                tr.setBuyIdentity(requestIdentity);
                tr.setSellIdentity(tradeIdentity);
            }

            return tradeRequestRep.save(tr).getId();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean updateTradeRequest(Long userId, TradeRequestDTO dto) {
        try {
            TradeRequest tr = tradeRequestRep.findById(dto.getTradeRequestId()).get();

            if (dto.getStatus() != null) {
                tr.setStatus(dto.getStatus());
            } else if (dto.getRate() != null) {
                if (userId.compareTo(tr.getBuyIdentity().getUser().getId()) == 0) {
                    tr.setBuyRate(dto.getRate());
                } else {
                    tr.setSellRate(dto.getRate());
                }
            }

            tradeRequestRep.save(tr);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public TradeRequestListDTO getTradeRequests(Long userId, CoinService.CoinEnum coinCode, Integer type, Integer index, Integer sort) {
        return null;
    }
}