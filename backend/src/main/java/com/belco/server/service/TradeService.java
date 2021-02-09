package com.belco.server.service;

import com.belco.server.dto.ChatMessageDTO;
import com.belco.server.dto.OrderDTO;
import com.belco.server.dto.TradeDTO;
import com.belco.server.dto.TradesDTO;
import com.belco.server.entity.Order;
import com.belco.server.entity.Trade;
import com.belco.server.entity.User;
import com.belco.server.entity.UserCoin;
import com.belco.server.model.*;
import com.belco.server.repository.OrderRep;
import com.belco.server.repository.TradeRep;
import com.belco.server.repository.UserCoinRep;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class TradeService {

    private final TradeRep tradeRep;
    private final OrderRep orderRep;
    private final UserCoinRep userCoinRep;
    private final UserService userService;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final MongoTemplate mongo;

    private Map<Long, TradeDTO> tradesMap = new ConcurrentHashMap<>();

    public TradeService(TradeRep tradeRep, OrderRep orderRep, UserCoinRep userCoinRep, UserService userService, SimpMessagingTemplate simpMessagingTemplate, MongoTemplate mongo) {
        this.tradeRep = tradeRep;
        this.orderRep = orderRep;
        this.userCoinRep = userCoinRep;
        this.userService = userService;
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.mongo = mongo;
    }

    @PostConstruct
    public void init() {
        tradesMap = tradeRep.findAllByStatus(TradeStatus.CREATED.getValue()).stream()
                .collect(Collectors.toMap(Trade::getId, trade -> trade.toDTO()));
    }

    public TradesDTO getTrades(Long userId) {
        try {
            User user = userService.findById(userId);

            List<TradeDTO> trades = tradesMap.values()
                    .stream().sorted(Comparator.comparing(TradeDTO::getPrice)).collect(Collectors.toList());

            List<OrderDTO> orders = orderRep.findAllByMakerOrTaker(user, user)
                    .stream().map(Order::toDTO).collect(Collectors.toList());

            return new TradesDTO(user.getIdentity().getPublicId(), user.getVerificationStatus(), user.getTotalTrades(), user.getTradingRate(), trades, orders);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new TradesDTO();
    }

    @Transactional
    public Response createTrade(Long userId, TradeDTO dto) {
        try {
            User user = userService.findById(userId);
            UserCoin userCoin = user.getUserCoin(dto.getCoin().name());
            Trade trade = new Trade();

            if (dto.getType() == TradeType.SELL) {
                BigDecimal lockedCryptoAmount = calculateLockedCryptoAmount(dto);

                if (userCoin.getReservedBalance().compareTo(lockedCryptoAmount) < 0) {
                    return Response.error(3, "Insufficient reserved balance");
                }

                userCoin.setReservedBalance(userCoin.getReservedBalance().subtract(lockedCryptoAmount).stripTrailingZeros());
                trade.setLockedCryptoAmount(lockedCryptoAmount);
            }

            userCoinRep.save(userCoin);

            trade.setType(dto.getType().getValue());
            trade.setStatus(dto.getStatus().getValue());
            trade.setPrice(dto.getPrice());
            trade.setMinLimit(dto.getMinLimit());
            trade.setMaxLimit(dto.getMaxLimit());
            trade.setPaymentMethods(dto.getPaymentMethods());
            trade.setTerms(dto.getTerms());
            trade.setCoin(dto.getCoin().getCoinEntity());
            trade.setMaker(user);

            trade = tradeRep.save(trade);
            tradesMap.put(trade.getId(), trade.toDTO());
            wsPushTrade(trade.toDTO());

            return Response.ok("id", trade.getId());
        } catch (Exception e) {
            e.printStackTrace();

            return Response.defaultError(e.getMessage());
        }
    }

    @Transactional
    public Response updateTrade(Long userId, TradeDTO dto) {
        try {
            User user = userService.findById(userId);
            UserCoin userCoin = user.getUserCoin(dto.getCoin().name());
            Trade trade = tradeRep.getOne(dto.getId());

            if (dto.getType() == TradeType.SELL) {
                userCoin.setReservedBalance(userCoin.getReservedBalance().add(trade.getLockedCryptoAmount()).stripTrailingZeros());
                trade.setLockedCryptoAmount(BigDecimal.ZERO);
                BigDecimal lockedCryptoAmount = calculateLockedCryptoAmount(dto);

                if (userCoin.getReservedBalance().compareTo(lockedCryptoAmount) < 0) {
                    return Response.error(3, "Insufficient reserved balance");
                }

                userCoin.setReservedBalance(userCoin.getReservedBalance().subtract(lockedCryptoAmount).stripTrailingZeros());
                trade.setLockedCryptoAmount(lockedCryptoAmount);
            } else if (dto.getType() == TradeType.BUY) {
                userCoin.setReservedBalance(userCoin.getReservedBalance().add(trade.getLockedCryptoAmount()).stripTrailingZeros());
                trade.setLockedCryptoAmount(BigDecimal.ZERO);
            }

            userCoinRep.save(userCoin);

            trade.setType(dto.getType().getValue());
            trade.setPrice(dto.getPrice());
            trade.setMinLimit(dto.getMinLimit());
            trade.setMaxLimit(dto.getMaxLimit());
            trade.setPaymentMethods(dto.getPaymentMethods());
            trade.setTerms(dto.getTerms());

            trade = tradeRep.save(trade);
            tradesMap.put(trade.getId(), trade.toDTO());
            wsPushTrade(trade.toDTO());

            return Response.ok("id", trade.getId());
        } catch (Exception e) {
            e.printStackTrace();

            return Response.defaultError(e.getMessage());
        }
    }

    @NotNull
    private BigDecimal calculateLockedCryptoAmount(TradeDTO dto) {
        return dto.getMaxLimit().divide(dto.getPrice()).stripTrailingZeros();
    }

    @Transactional
    public Response cancelTrade(Long userId, Long tradeId) {
        try {
            Trade trade = tradeRep.getOne(tradeId);

            if (trade.getOrders().stream().anyMatch(o -> o.getOrderStatus() != OrderStatus.RELEASED && o.getOrderStatus() != OrderStatus.SOLVED)) {
                return Response.error(2, "Trade contains open orders");
            }

            UserCoin userCoin = userService.findById(userId).getUserCoin(trade.getCoin().getCode());
            userCoin.setReservedBalance(userCoin.getReservedBalance().add(trade.getLockedCryptoAmount()).stripTrailingZeros());
            userCoinRep.save(userCoin);

            trade.setStatus(TradeStatus.CANCELED.getValue());
            trade.setLockedCryptoAmount(BigDecimal.ZERO);
            tradesMap.remove(trade.getId());
            trade = tradeRep.save(trade);
            wsPushTrade(trade.toDTO());

            return Response.ok(trade != null);
        } catch (Exception e) {
            e.printStackTrace();

            return Response.defaultError(e.getMessage());
        }
    }

    @Transactional
    public Response createOrder(Long userId, OrderDTO dto) {
        try {
            User taker = userService.findById(userId);
            UserCoin userCoin = taker.getUserCoin(dto.getCoin().name());
            Trade trade = tradeRep.getOne(dto.getTradeId());

            if (trade.getTradeStatus() == TradeStatus.CANCELED) {
                return Response.error(4, "Trade is canceled");
            }

            if (trade.getTradeType() == TradeType.BUY) {
                if (userCoin.getReservedBalance().compareTo(dto.getCryptoAmount()) < 0) {
                    return Response.error(3, "Insufficient reserved balance");
                }

                userCoin.setReservedBalance(userCoin.getReservedBalance().subtract(dto.getCryptoAmount()).stripTrailingZeros());
                userCoinRep.save(userCoin);
            }

            trade.setMaxLimit(trade.getMaxLimit().subtract(dto.getFiatAmount()).stripTrailingZeros());
            tradesMap.get(trade.getId()).setMaxLimit(trade.getMaxLimit());
            trade = tradeRep.save(trade);
            wsPushTrade(trade.toDTO());

            Order order = new Order();
            order.setStatus(OrderStatus.NEW.getValue());
            order.setPrice(dto.getPrice());
            order.setCryptoAmount(dto.getCryptoAmount());
            order.setFiatAmount(dto.getFiatAmount());
            order.setTerms(dto.getTerms());
            order.setTrade(trade);
            order.setCoin(dto.getCoin().getCoinEntity());
            order.setTrade(trade);
            order.setMaker(userService.findById(dto.getMakerId()));
            order.setTaker(taker);
            order.setCreateDate(new Date());

            order = orderRep.save(order);
            wsPushOrder(order.getMaker().getPhone(), order.toDTO());
            wsPushOrder(order.getTaker().getPhone(), order.toDTO());

            return Response.ok("id", order.getId());
        } catch (Exception e) {
            e.printStackTrace();

            return Response.defaultError(e.getMessage());
        }
    }

    @Transactional
    public Response updateOrder(Long userId, OrderDTO dto) {
        try {
            Order order = orderRep.getOne(dto.getId());
            Trade trade = order.getTrade();

            if(dto.getTradeRate() != null) {
                order.setMakerRate(dto.getTradeRate());

                if(userId.compareTo(order.getMaker().getId()) == 0) {
                    recalculateTradingData(order.getTaker(), dto.getTradeRate());
                } else {
                    recalculateTradingData(order.getMaker(), dto.getTradeRate());
                }
            } else if(dto.getStatus() != null) {
                if (dto.getStatus() == OrderStatus.RELEASED) {
                    if (trade.getTradeType() == TradeType.BUY) {
                        UserCoin userCoin = order.getMaker().getUserCoin(order.getCoin().getCode());
                        userCoin.setReservedBalance(userCoin.getReservedBalance().add(order.getCryptoAmount()).multiply(new BigDecimal("0.98")).stripTrailingZeros());
                    } else if (trade.getTradeType() == TradeType.SELL) {
                        UserCoin userCoin = order.getTaker().getUserCoin(order.getCoin().getCode());
                        userCoin.setReservedBalance(userCoin.getReservedBalance().add(order.getCryptoAmount()).multiply(new BigDecimal("0.98")).stripTrailingZeros());
                    }
                }

                order.setStatus(dto.getStatus().getValue());
            }

            order = orderRep.save(order);

            wsPushOrder(order.getMaker().getPhone(), order.toDTO());
            wsPushOrder(order.getTaker().getPhone(), order.toDTO());

            return Response.ok(order != null);
        } catch (Exception e) {
            e.printStackTrace();

            return Response.defaultError(e.getMessage());
        }
    }

    private void recalculateTradingData(User user, Integer tradeRate) {
        user.setTradingRate(user.getTradingRate().multiply(BigDecimal.valueOf(user.getTotalTrades())).add(BigDecimal.valueOf(tradeRate)).divide(BigDecimal.valueOf(user.getTotalTrades()).add(BigDecimal.ONE)).stripTrailingZeros());
        user.setTotalTrades(user.getTotalTrades() + 1);

        userService.save(user);
    }

    @Transactional
    public Response cancelOrder(Long userId, Long orderId) {
        try {
            Order order = orderRep.getOne(orderId);
            Trade trade = order.getTrade();

            if (trade.getTradeType() == TradeType.BUY) {
                UserCoin userCoin = order.getTaker().getUserCoin(order.getCoin().getCode());
                userCoin.setReservedBalance(userCoin.getReservedBalance().add(order.getCryptoAmount()));
                userCoinRep.save(userCoin);
            }

            trade.setMaxLimit(trade.getMaxLimit().add(order.getFiatAmount()).stripTrailingZeros());
            tradesMap.get(trade.getId()).setMaxLimit(trade.getMaxLimit());
            trade = tradeRep.save(trade);

            wsPushTrade(trade.toDTO());
            wsPushOrder(order.getMaker().getPhone(), order.toDTO());
            wsPushOrder(order.getTaker().getPhone(), order.toDTO());

            return Response.ok(trade != null);
        } catch (Exception e) {
            e.printStackTrace();

            return Response.defaultError(e.getMessage());
        }
    }

    public void processMessage(ChatMessageDTO dto) {
        try {
            mongo.save(dto);

            wsPushChatMessage(userService.findById(dto.getRecipientId()).getPhone(), dto);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void wsPushTrade(TradeDTO dto) {
        simpMessagingTemplate.convertAndSend("/topic/trade", dto);
    }

    public void wsPushOrder(String phone, OrderDTO dto) {
        simpMessagingTemplate.convertAndSendToUser(phone, "/queue/order", dto);
    }

    public void wsPushChatMessage(String phone, ChatMessageDTO dto) {
        simpMessagingTemplate.convertAndSendToUser(phone, "/queue/order-chat", dto);
    }
}