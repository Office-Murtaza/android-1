package com.batm.service;

import com.batm.dto.TradeDTO;
import com.batm.dto.TradeDetailsDTO;
import com.batm.dto.TradeListDTO;
import com.batm.dto.TradeRequestDTO;
import com.batm.entity.Coin;
import com.batm.entity.Trade;
import com.batm.entity.TradeRequest;
import com.batm.entity.User;
import com.batm.model.TradeSort;
import com.batm.model.TradeStatus;
import com.batm.model.TradeType;
import com.batm.repository.TradeRep;
import com.batm.repository.TradeRequestRep;
import com.batm.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

@Service
public class TradeService {

    @Autowired
    private TradeRep tradeRep;

    @Autowired
    private TradeRequestRep tradeRequestRep;

    @Autowired
    private UserService userService;

    public Long postTrade(Long userId, CoinService.CoinEnum coinCode, TradeDTO dto) {
        try {
            Trade trade;

            if (dto.getId() == null) {
                trade = new Trade();
                trade.setIdentity(userService.findById(userId).getIdentity());
                trade.setCoin(coinCode.getCoinEntity());
            } else {
                trade = tradeRep.findById(dto.getId()).get();
            }

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

    public Long postTradeRequest(Long userId, CoinService.CoinEnum coinCode, TradeRequestDTO dto) {
        try {
            Trade trade = tradeRep.findById(dto.getTradeId()).get();

            TradeRequest tr = new TradeRequest();
            tr.setTrade(trade);
            tr.setType(TradeType.getRequestType(trade.getType()));
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
            tr.setRequestIdentity(userService.findByUserId(userId));
            tr.setTradeIdentity(trade.getIdentity());

            return tradeRequestRep.save(tr).getId();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void deleteTrade(Long id) {
        tradeRep.deleteById(id);
    }

    public TradeListDTO getTrades(Long userId, CoinService.CoinEnum coinCode, Integer type, Integer index, Integer sort) {
        int page = index / 10;

        User user = userService.findById(userId);
        Coin coin = coinCode.getCoinEntity();
        TradeListDTO dto = new TradeListDTO();
        dto.setBuyTotal(tradeRep.countTradeByCoinAndType(coin, TradeType.BUY.getValue()));
        dto.setSellTotal(tradeRep.countTradeByCoinAndType(coin, TradeType.SELL.getValue()));

        sort = user.getLatitude() == null ? TradeSort.PRICE.getValue() : sort;

        if(type == null || type == TradeType.BUY.getValue()) {
            if(sort == TradeSort.PRICE.getValue()) {
                List<Trade> buyTrades = tradeRep.findAllByCoinAndTypeOrderByMarginAsc(coin, TradeType.BUY.getValue(), PageRequest.of(page, 10));
                dto.setBuyTrades(getTradeDetailsList(buyTrades, coinCode));
            } else {
                List<Trade> buyTrades = tradeRep.findAllByCoinAndType(coin, TradeType.BUY.getValue());

                buyTrades.stream().forEach(e -> e.setDistance(Util.distance(user, e.getIdentity().getUser())));
                buyTrades.sort(Comparator.comparing(Trade::getDistance));

                dto.setBuyTrades(getTradeDetailsList(buyTrades.subList(index - 1, index + 10 - 1), coinCode));
            }
        }

        if(type == null || type == TradeType.SELL.getValue()) {
            if(sort == TradeSort.PRICE.getValue()) {
                List<Trade> sellTrades = tradeRep.findAllByCoinAndTypeOrderByMarginDesc(coin, TradeType.SELL.getValue(), PageRequest.of(page, 10));
                dto.setSellTrades(getTradeDetailsList(sellTrades, coinCode));
            } else {
                List<Trade> buyTrades = tradeRep.findAllByCoinAndType(coin, TradeType.SELL.getValue());

                buyTrades.stream().forEach(e -> e.setDistance(Util.distance(user, e.getIdentity().getUser())));
                buyTrades.sort(Comparator.comparing(Trade::getDistance));

                dto.setBuyTrades(getTradeDetailsList(buyTrades.subList(index - 1, index + 10 - 1), coinCode));
            }
        }

        return dto;
    }

    private List<TradeDetailsDTO> getTradeDetailsList(List<Trade> trades, CoinService.CoinEnum coinCode) {
        List<TradeDetailsDTO> list = new LinkedList<>();

        for (int i = 0; i < trades.size(); i++) {
            Trade trade = trades.get(i);

            TradeDetailsDTO details = new TradeDetailsDTO();
            details.setId(trade.getId());
            details.setUsername(trade.getIdentity().getPublicId());
            details.setTradeCount(trade.getTradeCount());
            details.setTradeRate(trade.getTradeRate());
            details.setDistance(trade.getDistance());
            details.setPrice(coinCode.getPrice()
                    .multiply(trade.getMargin().divide(new BigDecimal(100)).add(BigDecimal.ONE))
                    .setScale(3, RoundingMode.DOWN).stripTrailingZeros());
            details.setPaymentMethod(trade.getPaymentMethod());
            details.setMinLimit(trade.getMinLimit());
            details.setMaxLimit(trade.getMaxLimit());
            details.setTerms(trade.getTerms());

            list.add(details);
        }

        return list;
    }
}