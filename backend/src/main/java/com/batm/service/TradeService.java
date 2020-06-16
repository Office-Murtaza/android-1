package com.batm.service;

import com.batm.dto.*;
import com.batm.entity.*;
import com.batm.model.TradeSort;
import com.batm.model.TradeRequestStatus;
import com.batm.model.TradeType;
import com.batm.repository.TradeRep;
import com.batm.repository.TradeRequestRep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

@Service
public class TradeService {

    private final int TAB_BUY = 1;
    private final int TAB_SELL = 2;
    private final int TAB_MY = 3;

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

    public TradeListDTO getTrades(Long userId, CoinService.CoinEnum coinCode, Integer tab, Integer index, Integer sort) {
        TradeListDTO dto = new TradeListDTO();

        try {
            User user = userService.findById(userId);
            Identity identity = user.getIdentity();
            Coin coin = coinCode.getCoinEntity();
            BigDecimal price = coinCode.getPrice();
            sort = user.getLatitude() == null ? TradeSort.PRICE.getValue() : sort;
            List<Trade> trades = new ArrayList<>();

            if (tab == TAB_BUY) {
                trades = tradeRep.findAllByCoinAndTypeAndIdentityNot(coin, TradeType.BUY.getValue(), identity);
            } else if (tab == TAB_SELL) {
                trades = tradeRep.findAllByCoinAndTypeAndIdentityNot(coin, TradeType.SELL.getValue(), identity);
            } else if (tab == TAB_MY) {
                trades = tradeRep.findAllByCoinAndIdentity(coin, identity);
            }

            List<TradeDetailsDTO> tradeDetails = new ArrayList<>();

            for (Trade tr : trades) {
                TradeUserDTO trader = new TradeUserDTO();
                trader.setUsername(tr.getIdentity().getPublicId());

                tr.setPrice(price.multiply(tr.getMargin().divide(new BigDecimal(100)).add(BigDecimal.ONE))
                        .setScale(3, RoundingMode.DOWN).stripTrailingZeros());

                if (tab == TAB_BUY || tab == TAB_SELL) {
                    //tr.setDistance(Util.distance(user, tr.getIdentity().getUser()));
                    trader.setDistance(new Random().nextInt(10));
                }

                trader.setTradeCount(new Random().nextInt(200));
                trader.setTradeRate(BigDecimal.valueOf(4.3));

                tradeDetails.add(tr.toDTO(trader));
            }

            dto.setTotal(tradeDetails.size());

            if ((tab == TAB_BUY || tab == TAB_SELL) && sort == TradeSort.DISTANCE.getValue()) {
                tradeDetails.sort(Comparator.comparing(e -> e.getTrader().getDistance()));
            } else {
                tradeDetails.sort(Comparator.comparing(TradeDetailsDTO::getPrice));
            }

            dto.setTrades(tradeDetails.subList(index - 1, Math.min(tradeDetails.size(), index + 10 - 1)));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return dto;
    }

    public Long createTradeRequest(Long userId, CoinService.CoinEnum coinCode, TradeRequestDTO dto) {
        try {
            Trade trade = tradeRep.findById(dto.getTradeId()).get();
            Identity tradeIdentity = trade.getIdentity();
            Identity requestIdentity = userService.findByUserId(userId);

            TradeRequest tr = new TradeRequest();
            tr.setStatus(TradeRequestStatus.NEW.getValue());
            tr.setCoin(coinCode.getCoinEntity());
            tr.setPaymentMethod(trade.getPaymentMethod());
            tr.setPrice(dto.getPrice());
            tr.setCryptoAmount(dto.getCryptoAmount());
            tr.setFiatAmount(dto.getFiatAmount());
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

    public TradeRequestListDTO getTradeRequests(Long userId, CoinService.CoinEnum coinCode, Integer index) {
        TradeRequestListDTO dto = new TradeRequestListDTO();

        try {
            Identity identity = userService.findByUserId(userId);
            Coin coin = coinCode.getCoinEntity();

            dto.setTotal(tradeRequestRep.countAllByCoinAndBuyIdentityOrSellIdentity(coin, identity, identity));
            List<TradeRequest> requests = tradeRequestRep.findAllByCoinAndBuyIdentityOrSellIdentityOrderByCreateDateDesc(coin, identity, identity, PageRequest.of(index / 10, 10));

            requests.stream().forEach(e -> {
                TradeUserDTO buyer = TradeUserDTO.builder()
                        .username(e.getBuyIdentity().getPublicId())
                        .distance(new Random().nextInt(10))
                        .tradeCount(new Random().nextInt(200))
                        .tradeRate(BigDecimal.valueOf(4.3))
                        .build();

                TradeUserDTO seller = TradeUserDTO.builder()
                        .username(e.getSellIdentity().getPublicId())
                        .distance(new Random().nextInt(10))
                        .tradeCount(new Random().nextInt(200))
                        .tradeRate(BigDecimal.valueOf(4.3))
                        .build();

                dto.getTradeRequests().add(e.toDTO(buyer, seller));
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        return dto;
    }
}