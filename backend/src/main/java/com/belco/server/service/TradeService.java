package com.belco.server.service;

import com.belco.server.dto.ChatMessageDTO;
import com.belco.server.dto.OrderDTO;
import com.belco.server.dto.TradeDTO;
import com.belco.server.dto.TradesDTO;
import com.belco.server.entity.Order;
import com.belco.server.entity.Trade;
import com.belco.server.entity.User;
import com.belco.server.entity.UserCoin;
import com.belco.server.model.OrderStatus;
import com.belco.server.model.Response;
import com.belco.server.model.TradeStatus;
import com.belco.server.model.TradeType;
import com.belco.server.repository.OrderRep;
import com.belco.server.repository.TradeRep;
import com.belco.server.repository.UserCoinRep;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.io.File;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class TradeService {

    private static final String COLL_ORDER_CHAT = "order_chat";

    private final TradeRep tradeRep;
    private final OrderRep orderRep;
    private final UserCoinRep userCoinRep;
    private final UserService userService;
    private final SocketService socketService;
    private final MongoTemplate mongo;

    //TODO: replace with Redis
    private Map<Long, TradeDTO> tradesMap = new ConcurrentHashMap<>();

    @Value("${upload.path.chat}")
    private String uploadPath;

    public TradeService(TradeRep tradeRep, OrderRep orderRep, UserCoinRep userCoinRep, UserService userService, SocketService socketService, MongoTemplate mongo) {
        this.tradeRep = tradeRep;
        this.orderRep = orderRep;
        this.userCoinRep = userCoinRep;
        this.userService = userService;
        this.socketService = socketService;
        this.mongo = mongo;
    }

    @PostConstruct
    public void init() {
        tradesMap = tradeRep.findAllByStatus(TradeStatus.ACTIVE.getValue()).stream()
                .collect(Collectors.toMap(Trade::getId, trade -> trade.toDTO()));
    }

    public Response getTrades(Long userId) {
        try {
            User user = userService.findById(userId);

            List<TradeDTO> trades = tradesMap.values()
                    .stream().sorted(Comparator.comparing(TradeDTO::getPrice)).collect(Collectors.toList());

            Map<Long, OrderDTO> orders = orderRep.findAllByMakerOrTaker(user, user)
                    .stream().collect(Collectors.toMap(Order::getId, Order::toDTO));

            mongo.getCollection(COLL_ORDER_CHAT).createIndex(new Document("orderId", 1));
            mongo.getCollection(COLL_ORDER_CHAT).find(new Document("orderId", new Document("$in", orders.keySet()))).into(new ArrayList<>()).stream().forEach(d -> orders.get(d.getLong("orderId")).getChat().add(ChatMessageDTO.toDTO(d)));

            return Response.ok(new TradesDTO(user.getIdentity().getPublicId(), user.getVerificationStatus(), user.getTotalTrades(), user.getTradingRate(), trades, new ArrayList<>(orders.values())));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @Transactional
    public Response createTrade(Long userId, TradeDTO dto) {
        try {
            User user = userService.findById(userId);
            user.setLatitude(dto.getMakerLatitude());
            user.setLongitude(dto.getMakerLongitude());
            user = userService.save(user);

            UserCoin userCoin = user.getUserCoin(dto.getCoin().name());
            Trade trade = new Trade();

            if (dto.getType() == TradeType.SELL) {
                BigDecimal lockedCryptoAmount = calculateLockedCryptoAmount(dto);

                if (userCoin.getReservedBalance().compareTo(lockedCryptoAmount) < 0) {
                    return Response.validationError("Insufficient reserved balance");
                }

                userCoin.setReservedBalance(userCoin.getReservedBalance().subtract(lockedCryptoAmount).stripTrailingZeros());
                trade.setLockedCryptoAmount(lockedCryptoAmount);
            }

            userCoinRep.save(userCoin);

            trade.setType(dto.getType().getValue());
            trade.setStatus(TradeStatus.ACTIVE.getValue());
            trade.setPrice(dto.getPrice());
            trade.setMinLimit(dto.getMinLimit());
            trade.setMaxLimit(dto.getMaxLimit());
            trade.setPaymentMethods(dto.getPaymentMethods());
            trade.setTerms(dto.getTerms());
            trade.setOpenOrders(0);
            trade.setCoin(dto.getCoin().getCoinEntity());
            trade.setMaker(user);

            trade = tradeRep.save(trade);

            TradeDTO rDTO = trade.toDTO();
            tradesMap.put(trade.getId(), rDTO);
            socketService.pushTrade(rDTO);

            return Response.ok(rDTO);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @Transactional
    public Response updateTrade(Long userId, TradeDTO dto) {
        try {
            Trade trade = tradeRep.getOne(dto.getId());
            User user = userService.findById(userId);
            UserCoin userCoin = user.getUserCoin(trade.getCoin().getCode());

            if (trade.getTradeType() == TradeType.SELL) {
                userCoin.setReservedBalance(userCoin.getReservedBalance().add(trade.getLockedCryptoAmount()).stripTrailingZeros());
                trade.setLockedCryptoAmount(BigDecimal.ZERO);
                BigDecimal lockedCryptoAmount = calculateLockedCryptoAmount(dto);

                if (userCoin.getReservedBalance().compareTo(lockedCryptoAmount) < 0) {
                    return Response.validationError("Insufficient reserved balance");
                }

                userCoin.setReservedBalance(userCoin.getReservedBalance().subtract(lockedCryptoAmount).stripTrailingZeros());
                trade.setLockedCryptoAmount(lockedCryptoAmount);

                userCoinRep.save(userCoin);
            } else if (trade.getTradeType() == TradeType.BUY) {
                trade.setLockedCryptoAmount(BigDecimal.ZERO);
            }

            trade.setPrice(dto.getPrice());
            trade.setMinLimit(dto.getMinLimit());
            trade.setMaxLimit(dto.getMaxLimit());
            trade.setPaymentMethods(dto.getPaymentMethods());
            trade.setTerms(dto.getTerms());

            trade = tradeRep.save(trade);

            TradeDTO rDTO = trade.toDTO();
            tradesMap.put(trade.getId(), rDTO);
            socketService.pushTrade(rDTO);

            return Response.ok(rDTO);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @Transactional
    public Response cancelTrade(Long userId, Long tradeId) {
        try {
            Trade trade = tradeRep.getOne(tradeId);

            if (trade.getOpenOrders() > 0) {
                return Response.validationError("Trade contains open orders");
            }

            UserCoin userCoin = userService.findById(userId).getUserCoin(trade.getCoin().getCode());
            userCoin.setReservedBalance(userCoin.getReservedBalance().add(trade.getLockedCryptoAmount()).stripTrailingZeros());
            userCoinRep.save(userCoin);

            trade.setLockedCryptoAmount(BigDecimal.ZERO);
            trade.setStatus(TradeStatus.CANCELED.getValue());
            tradesMap.remove(trade.getId());

            trade = tradeRep.save(trade);
            TradeDTO rDTO = trade.toDTO();
            socketService.pushTrade(rDTO);

            return Response.ok(rDTO);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @Transactional
    public Response createOrder(Long userId, OrderDTO dto) {
        try {
            Trade trade = tradeRep.getOne(dto.getTradeId());
            User taker = userService.findById(userId);
            UserCoin userCoin = taker.getUserCoin(trade.getCoin().getCode());

            if (trade.getTradeStatus() == TradeStatus.CANCELED) {
                return Response.validationError("Trade is canceled");
            }

            if (trade.getTradeType() == TradeType.BUY) {
                if (userCoin.getReservedBalance().compareTo(dto.getCryptoAmount()) < 0) {
                    return Response.validationError("Insufficient reserved balance");
                }

                userCoin.setReservedBalance(userCoin.getReservedBalance().subtract(dto.getCryptoAmount()).stripTrailingZeros());
                userCoinRep.save(userCoin);
            }

            trade.setMaxLimit(trade.getMaxLimit().subtract(dto.getFiatAmount()).stripTrailingZeros());
            trade.setOpenOrders(trade.getOpenOrders() + 1);
            tradesMap.get(trade.getId()).setMaxLimit(trade.getMaxLimit());
            tradesMap.get(trade.getId()).setOpenOrders(trade.getOpenOrders());
            trade = tradeRep.save(trade);
            socketService.pushTrade(trade.toDTO());

            Order order = new Order();
            order.setStatus(OrderStatus.NEW.getValue());
            order.setPrice(dto.getPrice());
            order.setCryptoAmount(dto.getCryptoAmount());
            order.setFiatAmount(dto.getFiatAmount());
            order.setTerms(dto.getTerms());
            order.setTrade(trade);
            order.setCoin(trade.getCoin());
            order.setTrade(trade);
            order.setMaker(trade.getMaker());
            order.setTaker(taker);
            order.setCreateDate(new Date());

            order = orderRep.save(order);
            OrderDTO rDTO = order.toDTO();
            socketService.pushOrder(order.getMaker().getPhone(), rDTO);
            socketService.pushOrder(order.getTaker().getPhone(), rDTO);

            return Response.ok(rDTO);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @Transactional
    public Response updateOrder(Long userId, OrderDTO dto) {
        try {
            Order order = orderRep.getOne(dto.getId());
            Trade trade = order.getTrade();

            //rate the trade
            if (dto.getRate() != null) {
                order.setMakerRate(dto.getRate());

                if (userId.compareTo(order.getMaker().getId()) == 0) {
                    recalculateTradingData(order.getTaker(), dto.getRate());
                } else {
                    recalculateTradingData(order.getMaker(), dto.getRate());
                }
            } else if (dto.getStatus() != null) {
                if (dto.getStatus() == OrderStatus.RELEASED || dto.getStatus() == OrderStatus.SOLVED) {
                    if (trade.getTradeType() == TradeType.BUY) {
                        UserCoin userCoin = order.getMaker().getUserCoin(order.getCoin().getCode());
                        userCoin.setReservedBalance(userCoin.getReservedBalance().add(order.getCryptoAmount()).multiply(new BigDecimal("0.98")).stripTrailingZeros());
                    } else if (trade.getTradeType() == TradeType.SELL) {
                        UserCoin userCoin = order.getTaker().getUserCoin(order.getCoin().getCode());
                        userCoin.setReservedBalance(userCoin.getReservedBalance().add(order.getCryptoAmount()).multiply(new BigDecimal("0.98")).stripTrailingZeros());
                    }
                }

                if (dto.getStatus() == OrderStatus.PAID || dto.getStatus() == OrderStatus.SOLVED) {
                    trade.setOpenOrders(trade.getOpenOrders() - 1);
                    tradesMap.get(trade.getId()).setOpenOrders(trade.getOpenOrders());
                    trade = tradeRep.save(trade);
                    socketService.pushTrade(trade.toDTO());
                }

                order.setStatus(dto.getStatus().getValue());
            }

            order = orderRep.save(order);
            OrderDTO rDTO = order.toDTO();
            socketService.pushOrder(order.getMaker().getPhone(), rDTO);
            socketService.pushOrder(order.getTaker().getPhone(), rDTO);

            return Response.ok(rDTO);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
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
            trade.setOpenOrders(trade.getOpenOrders() - 1);
            tradesMap.get(trade.getId()).setMaxLimit(trade.getMaxLimit());
            tradesMap.get(trade.getId()).setOpenOrders(trade.getOpenOrders());
            trade = tradeRep.save(trade);
            socketService.pushTrade(trade.toDTO());

            order.setStatus(OrderStatus.CANCELED.getValue());
            order = orderRep.save(order);
            OrderDTO rDTO = order.toDTO();
            socketService.pushOrder(order.getMaker().getPhone(), rDTO);
            socketService.pushOrder(order.getTaker().getPhone(), rDTO);

            if (userId.equals(order.getMaker().getId())) {
                //send push notification to taker
            } else {
                //send push notification to maker
            }

            return Response.ok(rDTO);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @Transactional
    public void processMessage(ChatMessageDTO dto) {
        try {
            if (StringUtils.isNotBlank(dto.getFileBase64())) {
                String newFileName = RandomStringUtils.randomAlphanumeric(20).toLowerCase() + "." + dto.getFileExtension();
                String newFilePath = uploadPath + File.separator + dto.getOrderId() + "_" + newFileName;

//                byte[] decodedBytes = Base64.getDecoder().decode(dto.getFileBase64());
//                FileUtils.writeByteArrayToFile(new File(newFilePath), decodedBytes);
//
                dto.setFilePath(newFilePath);
            }

            mongo.getCollection("order_chat").insertOne(dto.toDocument());
            User user = userService.findById(dto.getToUserId());

            socketService.pushChatMessage(user.getPhone(), dto);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void recalculateTradingData(User user, Integer tradeRate) {
        user.setTradingRate(user.getTradingRate().multiply(BigDecimal.valueOf(user.getTotalTrades())).add(BigDecimal.valueOf(tradeRate)).divide(BigDecimal.valueOf(user.getTotalTrades()).add(BigDecimal.ONE)).stripTrailingZeros());
        user.setTotalTrades(user.getTotalTrades() + 1);

        userService.save(user);
    }

    @NotNull
    private BigDecimal calculateLockedCryptoAmount(TradeDTO dto) {
        return dto.getMaxLimit().divide(dto.getPrice()).stripTrailingZeros();
    }
}