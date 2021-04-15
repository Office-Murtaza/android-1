package com.belco.server.service;

import com.belco.server.dto.*;
import com.belco.server.entity.User;
import com.belco.server.entity.UserCoin;
import com.belco.server.model.OrderStatus;
import com.belco.server.model.Response;
import com.belco.server.model.TradeStatus;
import com.belco.server.model.TradeType;
import com.belco.server.repository.UserCoinRep;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class TradeService {

    private final UserCoinRep userCoinRep;
    private final UserService userService;
    private final NotificationService notificationService;
    private final SocketService socketService;
    private final MongoTemplate mongo;

    public TradeService(UserCoinRep userCoinRep, UserService userService, NotificationService notificationService, SocketService socketService, MongoTemplate mongo) {
        this.userCoinRep = userCoinRep;
        this.userService = userService;
        this.notificationService = notificationService;
        this.socketService = socketService;
        this.mongo = mongo;
    }

    public Response getTradeHistory(Long userId) {
        try {
            User user = userService.findById(userId);

            List<TradeDetailsDTO> trades = mongo.find(new Query(Criteria.where("status").ne(TradeStatus.CANCELED.getValue())), TradeDetailsDTO.class);
            List<OrderDetailsDTO> orders = mongo.find(new Query(new Criteria().orOperator(Criteria.where("takerUserId").is(userId), Criteria.where("makerUserId").is(userId))), OrderDetailsDTO.class);
            List<OrderMessageDTO> messages = mongo.find(new Query(new Criteria().orOperator(Criteria.where("fromUserId").is(userId), Criteria.where("toUserId").is(userId))), OrderMessageDTO.class);

            TradeHistoryDTO dto = new TradeHistoryDTO();
            dto.setMakerPublicId(user.getIdentity().getPublicId());
            dto.setMakerStatus(user.getStatus());
            dto.setMakerTotalTrades(user.getTotalTrades());
            dto.setMakerTradingRate(user.getTradingRate());
            dto.setTrades(trades);
            dto.setOrders(orders);
            dto.setMessages(messages);

            return Response.ok(dto);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @Transactional
    public Response createTrade(Long userId, TradeDTO dto) {
        try {
            User user = userService.findById(userId);
            TradeDetailsDTO trade = new TradeDetailsDTO();

            if (dto.getType() == TradeType.SELL) {
                UserCoin userCoin = user.getUserCoin(dto.getCoin().name());
                if (lock(dto, trade, userCoin)) return Response.validationError("Insufficient reserved balance");
            }

            trade.setCoin(dto.getCoin().name());
            trade.setType(dto.getType().getValue());
            trade.setStatus(TradeStatus.ACTIVE.getValue());
            trade.setPrice(dto.getPrice());
            trade.setMinLimit(dto.getMinLimit());
            trade.setMaxLimit(dto.getMaxLimit());
            trade.setPaymentMethods(dto.getPaymentMethods());
            trade.setTerms(dto.getTerms());
            trade.setOpenOrders(0);
            trade.setMakerUserId(user.getId());
            trade.setMakerPublicId(user.getIdentity().getPublicId());
            trade.setMakerStatus(user.getStatus());
            trade.setMakerLatitude(user.getLatitude());
            trade.setMakerLongitude(user.getLongitude());
            trade.setMakerTotalTrades(user.getTotalTrades());
            trade.setMakerTradingRate(user.getTradingRate());
            trade.setTimestamp(System.currentTimeMillis());
            trade = mongo.save(trade);

            //redisTemplate.opsForHash().put(REDIS_TRADE, trade.getId(), trade);
            socketService.pushTrade(trade);

            return Response.ok("id", trade.getId());
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @Transactional
    public Response updateTrade(Long userId, TradeDTO dto) {
        try {
            TradeDetailsDTO trade = mongo.findOne(new Query(Criteria.where("_id").is(dto.getId())), TradeDetailsDTO.class);
            User user = userService.findById(userId);
            UserCoin userCoin = user.getUserCoin(trade.getCoin());

            if (trade.getType() == TradeType.SELL.getValue()) {
                userCoin.setReservedBalance(userCoin.getReservedBalance().add(trade.getLockedCryptoAmount()).stripTrailingZeros());
                trade.setLockedCryptoAmount(BigDecimal.ZERO);

                if (lock(dto, trade, userCoin)) return Response.validationError("Insufficient reserved balance");
            } else if (trade.getType() == TradeType.BUY.getValue()) {
                trade.setLockedCryptoAmount(BigDecimal.ZERO);
            }

            trade.setPrice(dto.getPrice());
            trade.setMinLimit(dto.getMinLimit());
            trade.setMaxLimit(dto.getMaxLimit());
            trade.setPaymentMethods(dto.getPaymentMethods());
            trade.setTerms(dto.getTerms());
            trade = mongo.save(trade);

            //redisTemplate.opsForHash().put(REDIS_TRADE, trade.getId(), trade);
            socketService.pushTrade(trade);

            return Response.ok(true);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @Transactional
    public Response cancelTrade(Long userId, String tradeId) {
        try {
            TradeDetailsDTO trade = mongo.findOne(new Query(Criteria.where("_id").is(tradeId)), TradeDetailsDTO.class);

            if (trade.getOpenOrders() > 0) {
                return Response.validationError("Trade contains open orders");
            }

            UserCoin userCoin = userService.findById(userId).getUserCoin(trade.getCoin());
            userCoin.setReservedBalance(userCoin.getReservedBalance().add(trade.getLockedCryptoAmount()).stripTrailingZeros());
            userCoinRep.save(userCoin);

            trade.setLockedCryptoAmount(BigDecimal.ZERO);
            trade.setStatus(TradeStatus.CANCELED.getValue());
            trade = mongo.save(trade);

            //redisTemplate.opsForHash().delete(REDIS_TRADE, tradeId);
            socketService.pushTrade(trade);

            return Response.ok(true);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @Transactional
    public Response createOrder(Long userId, OrderDTO dto) {
        try {
            TradeDetailsDTO trade = mongo.findOne(new Query(Criteria.where("_id").is(dto.getTradeId())), TradeDetailsDTO.class);
            User maker = userService.findById(trade.getMakerUserId());
            User taker = userService.findById(userId);
            UserCoin userCoin = taker.getUserCoin(trade.getCoin());

            if (trade.getStatus() == TradeStatus.CANCELED.getValue()) {
                return Response.validationError("Trade is canceled");
            }

            if (trade.getType() == TradeType.BUY.getValue()) {
                if (userCoin.getReservedBalance().compareTo(dto.getCryptoAmount()) < 0) {
                    return Response.validationError("Insufficient reserved balance");
                }

                userCoin.setReservedBalance(userCoin.getReservedBalance().subtract(dto.getCryptoAmount()).stripTrailingZeros());
                userCoinRep.save(userCoin);
            }

            trade.setMaxLimit(trade.getMaxLimit().subtract(dto.getFiatAmount()).stripTrailingZeros());
            trade.setOpenOrders(trade.getOpenOrders() + 1);
            trade = mongo.save(trade);

            //redisTemplate.opsForHash().put(REDIS_TRADE, trade.getId(), trade);
            socketService.pushTrade(trade);

            OrderDetailsDTO order = new OrderDetailsDTO();
            order.setTradeId(trade.getId());
            order.setCoin(trade.getCoin());
            order.setStatus(OrderStatus.NEW.getValue());
            order.setPrice(dto.getPrice());
            order.setCryptoAmount(dto.getCryptoAmount());
            order.setFiatAmount(dto.getFiatAmount());
            order.setTerms(trade.getTerms());
            order.setMakerUserId(maker.getId());
            order.setMakerPublicId(maker.getIdentity().getPublicId());
            order.setMakerStatus(maker.getStatus());
            order.setMakerLatitude(maker.getLatitude());
            order.setMakerLongitude(maker.getLongitude());
            order.setMakerTotalTrades(maker.getTotalTrades());
            order.setMakerTradingRate(maker.getTradingRate());
            order.setTakerUserId(taker.getId());
            order.setTakerPublicId(taker.getIdentity().getPublicId());
            order.setTakerStatus(taker.getStatus());
            order.setTakerLatitude(taker.getLatitude());
            order.setTakerLongitude(taker.getLongitude());
            order.setTakerTotalTrades(taker.getTotalTrades());
            order.setTakerTradingRate(taker.getTradingRate());
            order.setTimestamp(System.currentTimeMillis());
            order = mongo.save(order);

            //redisTemplate.opsForHash().put(REDIS_ORDER, order.getId(), order);
            socketService.pushOrder(maker.getPhone(), order);
            socketService.pushOrder(taker.getPhone(), order);

            notificationService.pushMessage("P2P Trade", "New Order was canceled", maker.getNotificationsToken());

            return Response.ok("id", order.getId());
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @Transactional
    public Response updateOrder(Long userId, OrderDTO dto) {
        try {
            OrderDetailsDTO order = mongo.findOne(new Query(Criteria.where("_id").is(dto.getId())), OrderDetailsDTO.class);
            TradeDetailsDTO trade = mongo.findOne(new Query(Criteria.where("_id").is(order.getTradeId())), TradeDetailsDTO.class);
            User maker = userService.findById(order.getMakerUserId());
            User taker = userService.findById(order.getTakerUserId());

            //rate the trade
            if (dto.getRate() != null) {
                order.setMakerRate(dto.getRate());

                if (userId.compareTo(order.getMakerUserId()) == 0) {
                    recalculateTradingData(order.getTakerUserId(), dto.getRate());
                } else {
                    recalculateTradingData(order.getMakerUserId(), dto.getRate());
                }
            } else if (dto.getStatus() != null) {
                if (dto.getStatus() == OrderStatus.RELEASED || dto.getStatus() == OrderStatus.SOLVED) {
                    if (trade.getType() == TradeType.BUY.getValue()) {
                        UserCoin userCoin = maker.getUserCoin(order.getCoin());
                        userCoin.setReservedBalance(userCoin.getReservedBalance().add(order.getCryptoAmount()).multiply(new BigDecimal("0.98")).stripTrailingZeros());
                    } else if (trade.getType() == TradeType.SELL.getValue()) {
                        UserCoin userCoin = taker.getUserCoin(order.getCoin());
                        userCoin.setReservedBalance(userCoin.getReservedBalance().add(order.getCryptoAmount()).multiply(new BigDecimal("0.98")).stripTrailingZeros());
                    }
                }

                if (dto.getStatus() == OrderStatus.PAID || dto.getStatus() == OrderStatus.SOLVED) {
                    trade.setOpenOrders(trade.getOpenOrders() - 1);
                    trade = mongo.save(trade);

                    //redisTemplate.opsForHash().put(REDIS_TRADE, trade.getId(), trade);
                    socketService.pushTrade(trade);
                }

                order.setStatus(dto.getStatus().getValue());
            }

            order = mongo.save(order);

            //redisTemplate.opsForHash().put(REDIS_ORDER, order.getId(), order);
            socketService.pushOrder(maker.getPhone(), order);
            socketService.pushOrder(taker.getPhone(), order);

            if (userId.equals(maker.getId())) {
                notificationService.pushMessage("P2P Trade", "Order was updated", taker.getNotificationsToken());
            } else {
                notificationService.pushMessage("P2P Trade", "Order was updated", maker.getNotificationsToken());
            }

            return Response.ok(true);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @Transactional
    public Response cancelOrder(Long userId, String orderId) {
        try {
            OrderDetailsDTO order = mongo.findOne(new Query(Criteria.where("_id").is(orderId)), OrderDetailsDTO.class);
            TradeDetailsDTO trade = mongo.findOne(new Query(Criteria.where("_id").is(order.getTradeId())), TradeDetailsDTO.class);
            User maker = userService.findById(order.getMakerUserId());
            User taker = userService.findById(order.getTakerUserId());

            if (trade.getType() == TradeType.BUY.getValue()) {
                UserCoin userCoin = taker.getUserCoin(order.getCoin());
                userCoin.setReservedBalance(userCoin.getReservedBalance().add(order.getCryptoAmount()));
                userCoinRep.save(userCoin);
            }

            trade.setMaxLimit(trade.getMaxLimit().add(order.getFiatAmount()).stripTrailingZeros());
            trade.setOpenOrders(trade.getOpenOrders() - 1);
            trade = mongo.save(trade);

            //redisTemplate.opsForHash().put(REDIS_TRADE, trade.getId(), trade);
            socketService.pushTrade(trade);

            order.setStatus(OrderStatus.CANCELED.getValue());
            order = mongo.save(order);

            //redisTemplate.opsForHash().put(REDIS_ORDER, order.getId(), order);
            socketService.pushOrder(maker.getPhone(), order);
            socketService.pushOrder(taker.getPhone(), order);

            if (userId.equals(maker.getId())) {
                notificationService.pushMessage("P2P Trade", "Order was canceled", taker.getNotificationsToken());
            } else {
                notificationService.pushMessage("P2P Trade", "Order was canceled", maker.getNotificationsToken());
            }

            return Response.ok(true);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @Transactional
    public void processMessage(OrderMessageDTO message) {
        try {
            message = mongo.save(message);
            //redisTemplate.opsForList().rightPush(REDIS_ORDER_CHAT + message.getFromUserId(), message);
            //redisTemplate.opsForList().rightPush(REDIS_ORDER_CHAT + message.getToUserId(), message);

            User user = userService.findById(message.getToUserId());
            socketService.pushChatMessage(user.getPhone(), message);
            notificationService.pushMessage("P2P Trade", "New trade message...", user.getNotificationsToken());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean lock(TradeDTO dto, TradeDetailsDTO trade, UserCoin userCoin) {
        BigDecimal lockedCryptoAmount = calculateLockedCryptoAmount(dto);

        if (userCoin.getReservedBalance().compareTo(lockedCryptoAmount) < 0) {
            return true;
        }

        userCoin.setReservedBalance(userCoin.getReservedBalance().subtract(lockedCryptoAmount).stripTrailingZeros());
        trade.setLockedCryptoAmount(lockedCryptoAmount);
        userCoinRep.save(userCoin);

        return false;
    }

    private void recalculateTradingData(Long userId, Integer tradeRate) {
        User user = userService.findById(userId);
        user.setTradingRate(user.getTradingRate().multiply(BigDecimal.valueOf(user.getTotalTrades())).add(BigDecimal.valueOf(tradeRate)).divide(BigDecimal.valueOf(user.getTotalTrades()).add(BigDecimal.ONE)).stripTrailingZeros());
        user.setTotalTrades(user.getTotalTrades() + 1);

        userService.save(user);
    }

    @NotNull
    private BigDecimal calculateLockedCryptoAmount(TradeDTO dto) {
        return dto.getMaxLimit().divide(dto.getPrice(), 6, RoundingMode.DOWN).stripTrailingZeros();
    }
}